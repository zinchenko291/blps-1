package me.zinch.itmo.mts.web.auth;

import me.zinch.itmo.mts.service.auth.AuthService;
import me.zinch.itmo.mts.service.auth.SessionUser;
import me.zinch.itmo.mts.web.auth.dto.AuthUserResponse;
import me.zinch.itmo.mts.web.auth.dto.LoginRequest;
import me.zinch.itmo.mts.web.auth.dto.RegisterManagerRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register manager account",
            description = "Who can call: currently open endpoint. Intended for MANAGER/SENIOR_MANAGER account bootstrap only."
    )
    public ResponseEntity<AuthUserResponse> register(
            @Valid @RequestBody RegisterManagerRequest request,
            HttpServletRequest servletRequest
    ) {
        SessionUser saved = authService.registerManager(request, servletRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthUserResponse(saved.id(), saved.login(), saved.name(), saved.role()));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login manager",
            description = "Who can call: MANAGER or SENIOR_MANAGER with valid login/password. On success user is stored in HTTP session."
    )
    public AuthUserResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        SessionUser sessionUser = authService.login(request, servletRequest);
        return new AuthUserResponse(sessionUser.id(), sessionUser.login(), sessionUser.name(), sessionUser.role());
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout current user",
            description = "Who can call: any authenticated session."
    )
    public Map<String, String> logout(HttpSession session) {
        authService.logout(session);
        return Map.of("message", "Logged out");
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get current session user",
            description = "Who can call: any authenticated session."
    )
    public AuthUserResponse me(HttpSession session) {
        SessionUser user = authService.getCurrentUser(session);
        return new AuthUserResponse(user.id(), user.login(), user.name(), user.role());
    }
}
