package me.zinch.itmo.mts.web.manager.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ManagerListItemResponse(
        @NotNull UUID managerId,
        @NotNull ManagerDto manager
) {
}
