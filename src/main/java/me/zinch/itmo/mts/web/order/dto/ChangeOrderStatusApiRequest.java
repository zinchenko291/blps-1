package me.zinch.itmo.mts.web.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import me.zinch.itmo.mts.domain.enums.OrderStatus;

@Getter
@Setter
public class ChangeOrderStatusApiRequest {

    @NotNull
    private OrderStatus status;
}
