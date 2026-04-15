package me.zinch.itmo.mts.web.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import me.zinch.itmo.mts.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrderApiResponse(
        @NotNull UUID id,
        @NotNull UUID customerId,
        @NotNull String customerName,
        @NotNull String customerEmail,
        @NotNull String customerPhoneNumber,
        UUID managerId,
        OrderManagerDto manager,
        @NotNull OrderStatus status,
        @NotNull OffsetDateTime createdAt,
        @NotNull OffsetDateTime updatedAt,
        @NotNull BigDecimal totalAmount,
        @NotNull List<@Valid OrderItemApiResponse> items
) {
}
