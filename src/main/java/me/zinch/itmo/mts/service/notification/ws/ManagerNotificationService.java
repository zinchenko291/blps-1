package me.zinch.itmo.mts.service.notification.ws;

import me.zinch.itmo.mts.domain.enums.OrderStatus;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerNotificationService {

    private static final String SENIOR_TOPIC = "/topic/notifications/senior-managers";
    private static final String MANAGER_TOPIC_PREFIX = "/topic/notifications/managers/";

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyNewOrderCreated(UUID orderId, OrderStatus status) {
        ManagerNotificationMessage payload = new ManagerNotificationMessage(
                ManagerNotificationType.NEW_ORDER_CREATED,
                orderId,
                null,
                status,
                OffsetDateTime.now()
        );
        send(SENIOR_TOPIC, payload);
    }

    public void notifyOrderAssigned(UUID orderId, UUID managerId, OrderStatus status) {
        ManagerNotificationMessage payload = new ManagerNotificationMessage(
                ManagerNotificationType.ORDER_ASSIGNED,
                orderId,
                managerId,
                status,
                OffsetDateTime.now()
        );
        send(MANAGER_TOPIC_PREFIX + managerId, payload);
    }

    public void notifyOrderStatusChangedForSeniorManagers(UUID orderId, UUID managerId, OrderStatus status) {
        ManagerNotificationMessage payload = new ManagerNotificationMessage(
                ManagerNotificationType.ORDER_STATUS_CHANGED,
                orderId,
                managerId,
                status,
                OffsetDateTime.now()
        );
        send(SENIOR_TOPIC, payload);
    }

    private void send(String destination, ManagerNotificationMessage payload) {
        try {
            messagingTemplate.convertAndSend(destination, payload);
        } catch (Exception e) {
            log.warn("Failed to send websocket notification to {}: {}", destination, e.getMessage());
        }
    }
}
