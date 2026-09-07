package me.zinch.itmo.mts.web.auth;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import me.zinch.itmo.mts.service.auth.AuthService;
import me.zinch.itmo.mts.service.auth.SessionUser;
import me.zinch.itmo.mts.web.auth.dto.AuthUserResponse;
import me.zinch.itmo.mts.web.auth.dto.LoginRequest;
import me.zinch.itmo.mts.security.JaasXmlAuthenticationProvider;
import me.zinch.itmo.mts.security.JwtService;
import me.zinch.itmo.mts.security.SecurityAccount;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JaasXmlAuthenticationProvider jaasAuthenticationProvider;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JaasXmlAuthenticationProvider jaasAuthenticationProvider,
            JwtService jwtService) {
        this.authService = authService;
        this.jaasAuthenticationProvider = jaasAuthenticationProvider;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public AuthUserResponse login(@Valid @RequestBody LoginRequest request) {
        var authentication = jaasAuthenticationProvider.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.getLogin(), request.getPassword()));
        SecurityAccount account = (SecurityAccount) authentication.getPrincipal();
        return new AuthUserResponse(account.id(), account.login(), account.name(), account.role(),
                jwtService.issue(account));
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public AuthUserResponse me(HttpSession session) {
        SessionUser user = authService.getCurrentUser(session);
        return new AuthUserResponse(user.id(), user.login(), user.name(), user.role(), null);
    }
}
