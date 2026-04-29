package me.zinch.itmo.mts.service.auth;

import jakarta.validation.constraints.NotNull;
import me.zinch.itmo.mts.domain.enums.UserRole;

import java.util.UUID;

public record SessionUser(
        @NotNull UUID id,
        @NotNull String login,
        @NotNull String name,
        @NotNull UserRole role
) {
}
