package me.zinch.itmo.mts.service.order.dto;

import java.util.List;

public record UpdateOrderRequest(
        String customerName,
        String phoneNumber,
        String email,
        List<OrderItemRequest> items
) {
}
