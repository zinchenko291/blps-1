package me.zinch.itmo.mts.web.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderItemApiRequest {

    @NotNull
    private UUID productId;

    @NotNull
    @Positive
    private Integer quantity;
}
