package me.zinch.itmo.mts.web.order.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemApiResponse(
        @NotNull UUID id,
        @NotNull UUID productId,
        @NotNull String productName,
        @NotNull Integer quantity,
        @NotNull BigDecimal unitPrice,
        @NotNull BigDecimal lineTotal
) {
}
