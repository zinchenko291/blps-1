package me.zinch.itmo.mts.service.notification;

import me.zinch.itmo.mts.domain.entity.Order;
import me.zinch.itmo.mts.domain.entity.OrderItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderEmailServiceImpl implements OrderEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Override
    public void sendOrderPlacedEmail(Order order, String paymentUrl, BigDecimal totalAmount) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(order.getCustomer().getEmail());
        message.setSubject("Заказ " + order.getId() + ": подтверждение и оплата");
        message.setText(buildBody(order, paymentUrl, totalAmount));
        mailSender.send(message);
    }

    private String buildBody(Order order, String paymentUrl, BigDecimal totalAmount) {
        StringBuilder builder = new StringBuilder();
        builder.append("Здравствуйте, ").append(order.getCustomer().getName()).append("!\n\n")
                .append("Ваш заказ согласован менеджером.\n")
                .append("Номер заказа: ").append(order.getId()).append("\n")
                .append("Состав заказа:\n");

        for (OrderItem item : order.getItems()) {
            BigDecimal lineTotal = item.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            builder.append("- ")
                    .append(item.getProduct().getName())
                    .append(", кол-во: ").append(item.getQuantity())
                    .append(", сумма: ").append(lineTotal.toPlainString())
                    .append('\n');
        }

        builder.append("\nИтого: ").append(totalAmount.setScale(2, RoundingMode.HALF_UP).toPlainString()).append('\n')
                .append("Ссылка на оплату: ").append(paymentUrl).append("\n\n")
                .append("Спасибо за заказ!");
        return builder.toString();
    }
}
