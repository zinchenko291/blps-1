package me.zinch.itmo.mts.web.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AssignManagerApiRequest {

    @NotNull
    private UUID managerId;
}
