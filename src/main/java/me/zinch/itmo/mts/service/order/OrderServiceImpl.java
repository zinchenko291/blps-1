package me.zinch.itmo.mts.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zinch.itmo.mts.config.YooKassaProperties;
import me.zinch.itmo.mts.domain.entity.*;
import me.zinch.itmo.mts.domain.payment.Payment;
import me.zinch.itmo.mts.domain.enums.OrderStatus;
import me.zinch.itmo.mts.domain.enums.UserRole;
import me.zinch.itmo.mts.domain.enums.YooKassaPaymentStatus;
import me.zinch.itmo.mts.repository.*;
import me.zinch.itmo.mts.repository.payment.PaymentRepository;
import me.zinch.itmo.mts.service.ServiceException;
import me.zinch.itmo.mts.service.notification.OrderEmailService;
import me.zinch.itmo.mts.service.order.dto.CreateOrderRequest;
import me.zinch.itmo.mts.service.order.dto.OrderItemRequest;
import me.zinch.itmo.mts.service.order.event.OrderApprovedEvent;
import me.zinch.itmo.mts.service.payment.CreatePaymentResult;
import me.zinch.itmo.mts.service.payment.YooKassaPaymentService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final OrderEmailService orderEmailService;
    private final ApplicationEventPublisher eventPublisher;
    private final YooKassaPaymentService yooKassaPaymentService;
    private final YooKassaProperties yooKassaProperties;
    private final TransactionTemplate jtaTransactionTemplate;

    @Override
    @PreAuthorize("hasAuthority('ORDER_CREATE')")
    public Order createOrder(CreateOrderRequest request) {
        Order savedOrder = jtaTransactionTemplate.execute(status -> {
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
            return orderRepository.save(order);
        });
        if (savedOrder == null) {
            throw new ServiceException("Не удалось создать заказ");
        }
        log.info("Order created: orderId={}, customerId={}, items={}",
                savedOrder.getId(), savedOrder.getCustomer().getId(), savedOrder.getItems().size());
        return savedOrder;
    }

    @Override
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public Order getOrderForUser(UUID userId, UUID orderId) {
        return jtaTransactionTemplate.execute(status -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ServiceException("Пользователь не найден: " + userId));
            Order order = loadOrder(orderId);
            if (user.getRole() == UserRole.SENIOR_MANAGER) return order;
            if (user.getRole() == UserRole.MANAGER) {
                assertManagedBy(order, userId);
                return order;
            }
            throw new ServiceException("Пользователь должен иметь роль MANAGER или SENIOR_MANAGER");
        });
    }

    @Override
    @PreAuthorize("hasAuthority('ORDER_UPDATE')")
    public Order updateOrder(UUID orderId, UUID managerId, CreateOrderRequest request) {
        return jtaTransactionTemplate.execute(status -> {
            requireRole(managerId, UserRole.MANAGER);
            validateCustomer(request.customerName(), request.phoneNumber(), request.email());
            List<OrderItemRequest> itemRequests = validateItems(request.items());
            Order order = loadOrder(orderId);
            assertManagedBy(order, managerId);
            assertStatusIsNew(order);
            Customer customer = upsertCustomer(request.customerName(), request.phoneNumber(), request.email());
            order.setCustomer(customer);
            replaceItems(order, itemRequests);
            order.setUpdatedAt(OffsetDateTime.now());
            return orderRepository.save(order);
        });
    }

    @Override
    @PreAuthorize("hasAuthority('ORDER_DELETE')")
    public void deleteOrder(UUID orderId, UUID seniorManagerId) {
        log.info("Deleting order: orderId={}, seniorManagerId={}", orderId, seniorManagerId);
        jtaTransactionTemplate.executeWithoutResult(status -> {
            requireRole(seniorManagerId, UserRole.SENIOR_MANAGER);
            if (paymentRepository.findByOrderId(orderId).isPresent()) {
                throw new ServiceException("Нельзя удалить заказ с созданной оплатой: " + orderId);
            }
            orderRepository.delete(loadOrder(orderId));
        });
    }

    @Override
    @PreAuthorize("hasAuthority('ORDER_LIST_OWN') or hasAuthority('ORDER_LIST_ALL')")
    public Page<Order> getOrdersForUser(UUID userId, Pageable pageable) {
        return jtaTransactionTemplate.execute(status -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ServiceException("Пользователь не найден: " + userId));
            if (user.getRole() == UserRole.SENIOR_MANAGER) return orderRepository.findAll(pageable);
            if (user.getRole() == UserRole.MANAGER) return orderRepository.findAllByManagerId(userId, pageable);
            throw new ServiceException("Пользователь должен иметь роль MANAGER или SENIOR_MANAGER");
        });
    }

    @Override
    @PreAuthorize("hasAuthority('ORDER_ASSIGN_MANAGER')")
    public Order assignManager(UUID orderId, UUID seniorManagerId, UUID managerId) {
        Order savedOrder = jtaTransactionTemplate.execute(status -> {
            log.info("Assigning manager to order: orderId={}, seniorManagerId={}, managerId={}",
                    orderId, seniorManagerId, managerId);
            requireRole(seniorManagerId, UserRole.SENIOR_MANAGER);
            User manager = requireRole(managerId, UserRole.MANAGER);
            Order order = loadOrder(orderId);
            order.setManager(manager);
            order.setUpdatedAt(OffsetDateTime.now());
            return orderRepository.save(order);
        });
        if (savedOrder == null) {
            throw new ServiceException("Не удалось назначить менеджера для заказа: " + orderId);
        }
        log.info("Manager assigned to order: orderId={}, managerId={}", savedOrder.getId(), managerId);
        return savedOrder;
    }

    @Override
    @PreAuthorize("hasAuthority('ORDER_CHANGE_STATUS')")
    public Order changeStatus(UUID orderId, UUID managerId, OrderStatus newStatus) {
        log.info("Changing order status: orderId={}, managerId={}, newStatus={}",
                orderId, managerId, newStatus);
        requireRole(managerId, UserRole.MANAGER);
        if (newStatus != OrderStatus.REJECTED && newStatus != OrderStatus.APPROVED) {
            throw new ServiceException("Недопустимое состояние для этого заказа");
        }

        Order savedOrder = jtaTransactionTemplate.execute(status -> {
            Order order = loadOrder(orderId);
            assertManagedBy(order, managerId);
            assertStatusIsNew(order);

            order.setStatus(newStatus);
            order.setUpdatedAt(OffsetDateTime.now());
            Order updatedOrder = orderRepository.save(order);

            return updatedOrder;
        });

        if (savedOrder == null) {
            throw new ServiceException("Не удалось изменить статус заказа: " + orderId);
        }

        log.info("Order status changed: orderId={}, managerId={}, status={}",
                savedOrder.getId(), managerId, newStatus);
        if (newStatus == OrderStatus.APPROVED) {
            eventPublisher.publishEvent(new OrderApprovedEvent(savedOrder.getId()));
        }
        return savedOrder;
    }

    /**
     * The payment provider call is deliberately made before the XA transaction: an HTTP call cannot
     * participate in two-phase commit. The resulting payment record and the CRM status change are
     * then committed (or rolled back) together by Atomikos.
     */
    @Override
    public Order requestPaymentLink(UUID orderId) {
        Order orderForPayment = loadOrder(orderId);
        assertStatusIsApproved(orderForPayment);

        BigDecimal totalAmount = calculateTotalAmount(orderForPayment);
        String idempotenceKey = UUID.randomUUID().toString();
        log.info("Requesting payment link: orderId={}, amount={}, currency={}",
                orderId, totalAmount, yooKassaProperties.currency());
        CreatePaymentResult paymentResult = yooKassaPaymentService.createPayment(
                orderForPayment, totalAmount, idempotenceKey);

        Order placedOrder = jtaTransactionTemplate.execute(status -> {
            Order order = loadOrder(orderId);
            assertStatusIsApproved(order);
            if (paymentRepository.findByOrderId(orderId).isPresent()) {
                throw new ServiceException("Оплата для заказа уже существует: " + orderId);
            }

            Payment payment = new Payment();
            payment.setOrderId(orderId);
            payment.setYooKassaPaymentId(paymentResult.yooKassaPaymentId());
            payment.setIdempotenceKey(idempotenceKey);
            payment.setAmount(totalAmount);
            payment.setCurrency(yooKassaProperties.currency());
            payment.setStatus(mapYooKassaStatus(paymentResult.rawStatus()));
            payment.setConfirmationUrl(paymentResult.confirmationUrl());
            payment.setReturnUrl(yooKassaProperties.returnUrl());
            payment.setCreatedAt(OffsetDateTime.now());
            paymentRepository.save(payment); // enlist mts_payments XA resource

            order.setStatus(OrderStatus.PLACED);
            order.setUpdatedAt(OffsetDateTime.now());
            return orderRepository.saveAndFlush(order); // enlist mts_crm XA resource before 2PC
        });

        if (placedOrder == null) {
            throw new ServiceException("Не удалось создать платёж для заказа: " + orderId);
        }
        orderEmailService.sendOrderPlacedEmail(placedOrder, paymentResult.confirmationUrl(), totalAmount);
        log.info("Payment and order committed in XA transaction: orderId={}, paymentId={}",
                orderId, paymentResult.yooKassaPaymentId());
        return placedOrder;
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

    private void assertStatusIsApproved(Order order) {
        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new ServiceException("Платёжную ссылку можно запросить только для заказа в статусе APPROVED");
        }
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
            if (item == null) {
                throw new ServiceException("Каждый элемент заказа должен содержать productId");
            }
            if (item.quantity() <= 0) {
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
