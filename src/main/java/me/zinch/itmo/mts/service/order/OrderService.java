package me.zinch.itmo.mts.service.order;

import me.zinch.itmo.mts.domain.entity.Order;
import me.zinch.itmo.mts.domain.enums.OrderStatus;
import me.zinch.itmo.mts.service.order.dto.CreateOrderRequest;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    Order createOrder(CreateOrderRequest request);

    List<Order> getAllOrders();

    List<Order> getOrdersAssignedToManager(UUID managerId);

    List<Order> getOrdersForUser(UUID userId);

    Order assignManager(UUID orderId, UUID seniorManagerId, UUID managerId);

    Order changeStatus(UUID orderId, UUID managerId, OrderStatus newStatus);
}
