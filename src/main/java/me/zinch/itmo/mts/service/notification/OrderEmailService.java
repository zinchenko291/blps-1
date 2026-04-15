package me.zinch.itmo.mts.service.notification;

import me.zinch.itmo.mts.domain.entity.Order;

import java.math.BigDecimal;

public interface OrderEmailService {
    void sendOrderPlacedEmail(Order order, String paymentUrl, BigDecimal totalAmount);
}
