package me.zinch.itmo.mts.service.order.dto;

import java.util.UUID;

public record OrderItemRequest(
        UUID productId,
        Integer quantity
) {
}
