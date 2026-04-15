package me.zinch.itmo.mts.web.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import me.zinch.itmo.mts.service.auth.AuthService;
import me.zinch.itmo.mts.service.auth.SessionUser;
import me.zinch.itmo.mts.web.auth.dto.AuthUserResponse;
import me.zinch.itmo.mts.web.auth.dto.LoginRequest;
import me.zinch.itmo.mts.web.auth.dto.RegisterManagerRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthUserResponse> register(
            @Valid @RequestBody RegisterManagerRequest request,
            HttpServletRequest servletRequest
    ) {
        SessionUser saved = authService.registerManager(request, servletRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthUserResponse(saved.id(), saved.login(), saved.name(), saved.role()));
    }

    @PostMapping("/login")
    public AuthUserResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        SessionUser sessionUser = authService.login(request, servletRequest);
        return new AuthUserResponse(sessionUser.id(), sessionUser.login(), sessionUser.name(), sessionUser.role());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public AuthUserResponse me(HttpSession session) {
        SessionUser user = authService.getCurrentUser(session);
        return new AuthUserResponse(user.id(), user.login(), user.name(), user.role());
    }
}
