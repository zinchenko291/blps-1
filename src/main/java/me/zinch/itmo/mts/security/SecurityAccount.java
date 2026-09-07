package me.zinch.itmo.mts.security;

import me.zinch.itmo.mts.domain.enums.UserRole;

import java.util.UUID;

public record SecurityAccount(UUID id, String login, String passwordHash, String name, UserRole role) {
}
