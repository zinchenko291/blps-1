package me.zinch.itmo.mts.web.manager;

import jakarta.servlet.http.HttpSession;
import me.zinch.itmo.mts.domain.entity.User;
import me.zinch.itmo.mts.service.auth.SessionAuthService;
import me.zinch.itmo.mts.service.auth.SessionUser;
import me.zinch.itmo.mts.service.manager.ManagerService;
import me.zinch.itmo.mts.web.manager.dto.ManagerDto;
import me.zinch.itmo.mts.web.manager.dto.ManagerListItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public Page<ManagerListItemResponse> getManagers(
            HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        SessionUser user = sessionAuthService.requireAuthenticated(session);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return managerService.getManagersForSenior(user.id(), pageable).map(this::toResponse);
    }

    private ManagerListItemResponse toResponse(User user) {
        return new ManagerListItemResponse(user.getId(), new ManagerDto(user.getName()));
    }
}
