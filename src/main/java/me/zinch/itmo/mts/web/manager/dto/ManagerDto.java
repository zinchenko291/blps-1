package me.zinch.itmo.mts.web.manager.dto;

import jakarta.validation.constraints.NotNull;

public record ManagerDto(
        @NotNull String name
) {
}
