package me.zinch.itmo.mts.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yookassa")
public record YooKassaProperties(
        String apiUrl,
        String shopId,
        String secretKey,
        Boolean capture,
        String currency,
        String returnUrl
) {
}
