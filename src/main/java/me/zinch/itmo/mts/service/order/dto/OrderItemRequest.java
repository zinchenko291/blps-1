package me.zinch.itmo.mts.service.order.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderItemRequest(
        @NotNull UUID productId,
        @NotNull Integer quantity
) {
}
