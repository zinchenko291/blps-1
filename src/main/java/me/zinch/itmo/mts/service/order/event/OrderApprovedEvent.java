package me.zinch.itmo.mts.service.order.event;

import java.util.UUID;

public record OrderApprovedEvent(UUID orderId) {
}
