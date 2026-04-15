package me.zinch.itmo.mts.web.order.dto;

import me.zinch.itmo.mts.domain.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeOrderStatusApiRequest {

    @NotNull
    private OrderStatus status;
}
