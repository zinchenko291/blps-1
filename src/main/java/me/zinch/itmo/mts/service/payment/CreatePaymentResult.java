package me.zinch.itmo.mts.service.payment;

public record CreatePaymentResult(
        String yooKassaPaymentId,
        String confirmationUrl,
        String rawStatus
) {
}
