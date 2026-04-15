package me.zinch.itmo.mts.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zinch.itmo.mts.config.YooKassaProperties;
import me.zinch.itmo.mts.domain.entity.*;
import me.zinch.itmo.mts.domain.enums.OrderStatus;
import me.zinch.itmo.mts.domain.enums.UserRole;
import me.zinch.itmo.mts.domain.enums.YooKassaPaymentStatus;
import me.zinch.itmo.mts.repository.*;
import me.zinch.itmo.mts.service.ServiceException;
import me.zinch.itmo.mts.service.notification.OrderEmailService;
import me.zinch.itmo.mts.service.notification.ws.event.NewOrderCreatedEvent;
import me.zinch.itmo.mts.service.notification.ws.event.OrderAssignedEvent;
import me.zinch.itmo.mts.service.notification.ws.event.OrderStatusChangedEvent;
import me.zinch.itmo.mts.service.order.dto.CreateOrderRequest;
import me.zinch.itmo.mts.service.order.dto.OrderItemRequest;
import me.zinch.itmo.mts.service.payment.CreatePaymentResult;
import me.zinch.itmo.mts.service.payment.YooKassaPaymentService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Sort CREATED_AT_DESC = Sort.by(Sort.Direction.DESC, "createdAt");
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final YooKassaPaymentService yooKassaPaymentService;
    private final OrderEmailService orderEmailService;
    private final ApplicationEventPublisher eventPublisher;
    private final YooKassaProperties yooKassaProperties;

    @Override
    public Order createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer email={}", request.email());
        validateCustomer(request.customerName(), request.phoneNumber(), request.email());
        List<OrderItemRequest> itemRequests = validateItems(request.items());

        Customer customer = upsertCustomer(request.customerName(), request.phoneNumber(), request.email());
        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.NEW);
        order.setCreatedAt(OffsetDateTime.now());
        order.setUpdatedAt(OffsetDateTime.now());

        replaceItems(order, itemRequests);
        Order savedOrder = orderRepository.save(order);
        log.info("Order created: orderId={}, customerId={}, items={}",
                savedOrder.getId(), savedOrder.getCustomer().getId(), savedOrder.getItems().size());
        eventPublisher.publishEvent(new NewOrderCreatedEvent(savedOrder.getId(), savedOrder.getStatus()));
        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        List<Order> orders = orderRepository.findAll(CREATED_AT_DESC);
        log.debug("Loaded all orders: count={}", orders.size());
        return orders;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersAssignedToManager(UUID managerId) {
        requireRole(managerId, UserRole.MANAGER);
        List<Order> orders = orderRepository.findAllByManagerId(managerId, CREATED_AT_DESC);
        log.debug("Loaded manager orders: managerId={}, count={}", managerId, orders.size());
        return orders;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersForUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException("Пользователь не найден: " + userId));
        if (user.getRole() == UserRole.SENIOR_MANAGER) {
            List<Order> orders = orderRepository.findAll(CREATED_AT_DESC);
            log.debug("Loaded orders for senior manager: userId={}, count={}", userId, orders.size());
            return orders;
        }
        if (user.getRole() == UserRole.MANAGER) {
            List<Order> orders = orderRepository.findAllByManagerId(userId, CREATED_AT_DESC);
            log.debug("Loaded orders for manager: userId={}, count={}", userId, orders.size());
            return orders;
        }
        throw new ServiceException("Пользователь должен иметь роль MANAGER или SENIOR_MANAGER");
    }

    @Override
    public Order assignManager(UUID orderId, UUID seniorManagerId, UUID managerId) {
        log.info("Assigning manager to order: orderId={}, seniorManagerId={}, managerId={}",
                orderId, seniorManagerId, managerId);
        requireRole(seniorManagerId, UserRole.SENIOR_MANAGER);
        User manager = requireRole(managerId, UserRole.MANAGER);
        Order order = loadOrder(orderId);

        order.setManager(manager);
        order.setUpdatedAt(OffsetDateTime.now());
        Order savedOrder = orderRepository.save(order);
        log.info("Manager assigned to order: orderId={}, managerId={}", savedOrder.getId(), managerId);
        eventPublisher.publishEvent(new OrderAssignedEvent(
                savedOrder.getId(),
                manager.getId(),
                savedOrder.getStatus()
        ));
        return savedOrder;
    }

    @Override
    public Order changeStatus(UUID orderId, UUID managerId, OrderStatus newStatus) {
        log.info("Changing order status: orderId={}, managerId={}, newStatus={}",
                orderId, managerId, newStatus);
        requireRole(managerId, UserRole.MANAGER);
        Order order = loadOrder(orderId);
        assertManagedBy(order, managerId);
        assertStatusIsNew(order);

        if (newStatus != OrderStatus.REJECTED && newStatus != OrderStatus.PLACED) {
            throw new ServiceException("Недопустимое состояние для этого заказа");
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(OffsetDateTime.now());
        Order savedOrder = orderRepository.save(order);

        if (newStatus == OrderStatus.PLACED) {
            createPaymentAndSendEmail(savedOrder);
        }

        log.info("Order status changed: orderId={}, managerId={}, status={}",
                savedOrder.getId(), managerId, newStatus);
        eventPublisher.publishEvent(new OrderStatusChangedEvent(
                savedOrder.getId(),
                savedOrder.getManager() == null ? null : savedOrder.getManager().getId(),
                newStatus
        ));
        return savedOrder;
    }

    private void replaceItems(Order order, List<OrderItemRequest> itemRequests) {
        order.getItems().clear();
        for (OrderItemRequest itemRequest : itemRequests) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ServiceException("Товар не найден: " + itemRequest.productId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.quantity());
            order.getItems().add(orderItem);
        }
    }

    private Customer upsertCustomer(String name, String phoneNumber, String email) {
        Customer customer = customerRepository.findByEmail(email).orElseGet(Customer::new);
        customer.setName(name);
        customer.setPhoneNumber(phoneNumber);
        customer.setEmail(email);
        return customerRepository.save(customer);
    }

    private User requireRole(UUID userId, UserRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException("Пользователь не найден: " + userId));
        if (user.getRole() != role) {
            throw new ServiceException("Пользователь " + userId + " должен иметь роль " + role);
        }
        return user;
    }

    private Order loadOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ServiceException("Заказ не найден: " + orderId));
    }

    private void assertManagedBy(Order order, UUID managerId) {
        if (order.getManager() == null || !order.getManager().getId().equals(managerId)) {
            throw new ServiceException("Заказ не назначен менеджеру: " + managerId);
        }
    }

    private void assertStatusIsNew(Order order) {
        if (order.getStatus() != OrderStatus.NEW) {
            throw new ServiceException("Заказ можно изменять только в статусе NEW");
        }
    }

    private void createPaymentAndSendEmail(Order order) {
        if (paymentRepository.findByOrderId(order.getId()).isPresent()) {
            throw new ServiceException("Оплата для заказа уже существует: " + order.getId());
        }

        BigDecimal totalAmount = calculateTotalAmount(order);
        String idempotenceKey = UUID.randomUUID().toString();
        log.info("Creating payment for order: orderId={}, amount={}, currency={}",
                order.getId(), totalAmount, yooKassaProperties.currency());
        CreatePaymentResult result = yooKassaPaymentService.createPayment(order, totalAmount, idempotenceKey);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setYooKassaPaymentId(result.yooKassaPaymentId());
        payment.setIdempotenceKey(idempotenceKey);
        payment.setAmount(totalAmount);
        payment.setCurrency(yooKassaProperties.currency());
        payment.setStatus(mapYooKassaStatus(result.rawStatus()));
        payment.setConfirmationUrl(result.confirmationUrl());
        payment.setReturnUrl(yooKassaProperties.returnUrl());
        payment.setCreatedAt(OffsetDateTime.now());
        paymentRepository.save(payment);
        log.info("Payment created for order: orderId={}, yooKassaPaymentId={}",
                order.getId(), result.yooKassaPaymentId());

        orderEmailService.sendOrderPlacedEmail(order, result.confirmationUrl(), totalAmount);
        log.info("Order payment email sent: orderId={}, email={}", order.getId(), order.getCustomer().getEmail());
    }

    private BigDecimal calculateTotalAmount(Order order) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            BigDecimal lineAmount = item.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(lineAmount);
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private YooKassaPaymentStatus mapYooKassaStatus(String status) {
        return switch (status) {
            case "pending" -> YooKassaPaymentStatus.PENDING;
            case "waiting_for_capture" -> YooKassaPaymentStatus.WAITING_FOR_CAPTURE;
            case "succeeded" -> YooKassaPaymentStatus.SUCCEEDED;
            case "canceled" -> YooKassaPaymentStatus.CANCELED;
            default -> throw new ServiceException("Неподдерживаемый статус YooKassa: " + status);
        };
    }

    private void validateCustomer(String customerName, String phoneNumber, String email) {
        if (isBlank(customerName) || isBlank(phoneNumber) || isBlank(email)) {
            throw new ServiceException("Необходимо указать имя, телефон и email покупателя");
        }
    }

    private List<OrderItemRequest> validateItems(List<OrderItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new ServiceException("Заказ должен содержать хотя бы один товар");
        }

        List<OrderItemRequest> validatedItems = new ArrayList<>(items.size());
        for (OrderItemRequest item : items) {
            if (item == null || item.productId() == null) {
                throw new ServiceException("Каждый элемент заказа должен содержать productId");
            }
            if (item.quantity() == null || item.quantity() <= 0) {
                throw new ServiceException("Количество должно быть положительным");
            }
            validatedItems.add(item);
        }
        return validatedItems;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
