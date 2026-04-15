package me.zinch.itmo.mts.service.payment;

import me.zinch.itmo.mts.config.YooKassaProperties;
import me.zinch.itmo.mts.domain.entity.Order;
import me.zinch.itmo.mts.service.ServiceException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class YooKassaPaymentServiceImpl implements YooKassaPaymentService {

    private final RestClient.Builder restClientBuilder;
    private final YooKassaProperties properties;

    @Override
    public CreatePaymentResult createPayment(Order order, BigDecimal amount, String idempotenceKey) {
        Map<String, Object> amountNode = Map.of(
                "value", amount.setScale(2).toPlainString(),
                "currency", properties.currency()
        );
        Map<String, Object> confirmationNode = Map.of(
                "type", "redirect",
                "return_url", properties.returnUrl()
        );
        Map<String, Object> metadataNode = Map.of(
                "orderId", order.getId().toString(),
                "customerEmail", order.getCustomer().getEmail()
        );

        Map<String, Object> payload = new HashMap<>();
        payload.put("amount", amountNode);
        payload.put("capture", properties.capture());
        payload.put("confirmation", confirmationNode);
        payload.put("description", "Оплата заказа " + order.getId());
        payload.put("metadata", metadataNode);

        Map<String, Object> response = restClientBuilder.build()
                .post()
                .uri(properties.apiUrl() + "/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> {
                    headers.setBasicAuth(properties.shopId(), properties.secretKey());
                    headers.add("Idempotence-Key", idempotenceKey);
                })
                .body(payload)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new ServiceException("YooKassa response is empty or invalid");
        }

        String paymentId = asString(response.get("id"));
        String status = asString(response.get("status"));
        String confirmationUrl = null;
        Object confirmation = response.get("confirmation");
        if (confirmation instanceof Map<?, ?> confirmationMap) {
            confirmationUrl = asString(confirmationMap.get("confirmation_url"));
        }
        if (paymentId == null || status == null || confirmationUrl == null) {
            throw new ServiceException("YooKassa response does not contain required payment fields");
        }

        return new CreatePaymentResult(paymentId, confirmationUrl, status);
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
