package me.zinch.itmo.mts.service.payment;

import me.zinch.itmo.mts.domain.entity.Order;

import java.math.BigDecimal;

public interface YooKassaPaymentService {
    CreatePaymentResult createPayment(Order order, BigDecimal amount, String idempotenceKey);
}
