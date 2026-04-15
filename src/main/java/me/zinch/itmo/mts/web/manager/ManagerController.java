package me.zinch.itmo.mts.web.manager;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import me.zinch.itmo.mts.domain.entity.User;
import me.zinch.itmo.mts.service.auth.SessionUser;
import me.zinch.itmo.mts.service.manager.ManagerService;
import me.zinch.itmo.mts.service.auth.SessionAuthService;
import me.zinch.itmo.mts.web.manager.dto.ManagerDto;
import me.zinch.itmo.mts.web.manager.dto.ManagerListItemResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/managers")
public class ManagerController {

    private final ManagerService managerService;
    private final SessionAuthService sessionAuthService;

    public ManagerController(ManagerService managerService, SessionAuthService sessionAuthService) {
        this.managerService = managerService;
        this.sessionAuthService = sessionAuthService;
    }

    @GetMapping
    @Operation(
            summary = "Get all managers",
            description = "Who can call: only SENIOR_MANAGER from active session. Returns users with role MANAGER only."
    )
    public List<ManagerListItemResponse> getManagers(HttpSession session) {
        SessionUser user = sessionAuthService.requireAuthenticated(session);
        return managerService.getManagersForSenior(user.id()).stream()
                .map(this::toResponse)
                .toList();
    }

    private ManagerListItemResponse toResponse(User user) {
        return new ManagerListItemResponse(user.getId(), new ManagerDto(user.getName()));
    }
}
