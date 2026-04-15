package me.zinch.itmo.mts.web.manager.dto;

import java.util.UUID;

public record ManagerListItemResponse(
        UUID managerId,
        ManagerDto manager
) {
}
