package me.zinch.itmo.mts.service.auth;

import me.zinch.itmo.mts.domain.entity.User;
import me.zinch.itmo.mts.domain.enums.UserRole;
import me.zinch.itmo.mts.repository.UserRepository;
import me.zinch.itmo.mts.service.ServiceException;
import me.zinch.itmo.mts.web.auth.dto.LoginRequest;
import me.zinch.itmo.mts.web.auth.dto.RegisterManagerRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final SessionAuthService sessionAuthService;

    @Override
    public SessionUser registerManager(RegisterManagerRequest request, HttpServletRequest servletRequest) {
        if (userRepository.findByLogin(request.getLogin()).isPresent()) {
            throw new ServiceException("Логин уже занят");
        }

        User user = User.builder()
                .login(request.getLogin())
                .password(passwordService.hash(request.getPassword()))
                .name(request.getName())
                .role(request.getRole())
                .build();
        User saved = userRepository.save(user);
        return sessionAuthService.authenticate(servletRequest, saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SessionUser login(LoginRequest request, HttpServletRequest servletRequest) {
        User user = userRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new UnauthorizedException("Неправильный пароль или логин"));
        if (!passwordService.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Неправильный пароль или логин");
        }
        return sessionAuthService.authenticate(servletRequest, user);
    }

    @Override
    public void logout(HttpSession session) {
        sessionAuthService.logout(session);
    }

    @Override
    public SessionUser getCurrentUser(HttpSession session) {
        return sessionAuthService.requireAuthenticated(session);
    }
}
