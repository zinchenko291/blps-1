package me.zinch.itmo.mts.web.auth.dto;

import jakarta.validation.constraints.NotNull;
import me.zinch.itmo.mts.domain.enums.UserRole;

import java.util.UUID;

public record AuthUserResponse(
        @NotNull UUID id,
        @NotNull String login,
        @NotNull String name,
        @NotNull UserRole role
) {
}
