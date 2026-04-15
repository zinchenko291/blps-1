package me.zinch.itmo.mts.service.notification.ws;

import lombok.RequiredArgsConstructor;
import me.zinch.itmo.mts.service.notification.ws.event.NewOrderCreatedEvent;
import me.zinch.itmo.mts.service.notification.ws.event.OrderAssignedEvent;
import me.zinch.itmo.mts.service.notification.ws.event.OrderStatusChangedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ManagerNotificationEventListener {

    private final ManagerNotificationService managerNotificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNewOrderCreated(NewOrderCreatedEvent event) {
        managerNotificationService.notifyNewOrderCreated(event.orderId(), event.status());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderAssigned(OrderAssignedEvent event) {
        managerNotificationService.notifyOrderAssigned(event.orderId(), event.managerId(), event.status());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        managerNotificationService.notifyOrderStatusChangedForSeniorManagers(
                event.orderId(),
                event.managerId(),
                event.status()
        );
    }
}
