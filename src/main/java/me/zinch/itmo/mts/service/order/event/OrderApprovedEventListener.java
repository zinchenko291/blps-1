package me.zinch.itmo.mts.service.order.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zinch.itmo.mts.service.order.OrderService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderApprovedEventListener {

    private final OrderService orderService;

    @EventListener
    public void onOrderApproved(OrderApprovedEvent event) {
        log.info("Starting payment-link service task: orderId={}", event.orderId());
        orderService.requestPaymentLink(event.orderId());
    }
}
