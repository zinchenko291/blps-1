package me.zinch.itmo.mts.service.auth;

import me.zinch.itmo.mts.domain.enums.UserRole;
import java.util.UUID;

public record SessionUser(
        UUID id,
        String login,
        String name,
        UserRole role
) {
}
