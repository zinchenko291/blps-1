package me.zinch.itmo.mts.web.auth.dto;

import me.zinch.itmo.mts.domain.enums.UserRole;
import java.util.UUID;

public record AuthUserResponse(
        UUID id,
        String login,
        String name,
        UserRole role
) {
}
