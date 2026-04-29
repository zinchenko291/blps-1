package me.zinch.itmo.mts.service.order.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotNull String customerName,
        @NotNull String phoneNumber,
        @NotNull String email,
        @NotNull List<OrderItemRequest> items
) {
}
