package me.zinch.itmo.mts.service.notification.ws.event;

import me.zinch.itmo.mts.domain.enums.OrderStatus;

import java.util.UUID;

public record OrderAssignedEvent(
        UUID orderId,
        UUID managerId,
        OrderStatus status
) {
}
