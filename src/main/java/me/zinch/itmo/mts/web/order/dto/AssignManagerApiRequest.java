package me.zinch.itmo.mts.web.order.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignManagerApiRequest {

    @NotNull
    private UUID managerId;
}
