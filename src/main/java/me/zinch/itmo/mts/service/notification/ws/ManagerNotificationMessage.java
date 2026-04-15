package me.zinch.itmo.mts.service.notification.ws;

import me.zinch.itmo.mts.domain.enums.OrderStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ManagerNotificationMessage(
        ManagerNotificationType type,
        UUID orderId,
        UUID managerId,
        OrderStatus status,
        OffsetDateTime timestamp
) {
}
