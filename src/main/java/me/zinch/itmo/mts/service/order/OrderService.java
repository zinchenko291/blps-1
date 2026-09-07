package me.zinch.itmo.mts.service.order;

import me.zinch.itmo.mts.domain.entity.Order;
import me.zinch.itmo.mts.domain.enums.OrderStatus;
import me.zinch.itmo.mts.service.order.dto.CreateOrderRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {

    Order createOrder(CreateOrderRequest request);

    Order getOrderForUser(UUID userId, UUID orderId);

    Order updateOrder(UUID orderId, UUID seniorManagerId, CreateOrderRequest request);

    void deleteOrder(UUID orderId, UUID seniorManagerId);

    Page<Order> getOrdersForUser(UUID userId, Pageable pageable);

    Order assignManager(UUID orderId, UUID seniorManagerId, UUID managerId);

    Order changeStatus(UUID orderId, UUID managerId, OrderStatus newStatus);

    Order requestPaymentLink(UUID orderId);
}
