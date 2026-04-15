package me.zinch.itmo.mts.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import me.zinch.itmo.mts.domain.enums.YooKassaPaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(unique = true, length = 64)
    private String yooKassaPaymentId;

    @Column(nullable = false, unique = true, length = 64)
    private String idempotenceKey;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private YooKassaPaymentStatus status;

    @Column(length = 1024)
    private String confirmationUrl;

    @Column(length = 1024)
    private String returnUrl;

    @Column(nullable = false)
    private OffsetDateTime createdAt;
}
