package me.zinch.itmo.mts.web;

import java.time.OffsetDateTime;

public record ErrorResponse(
        String message,
        OffsetDateTime timestamp
) {
}
