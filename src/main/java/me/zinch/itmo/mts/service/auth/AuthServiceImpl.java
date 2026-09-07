package me.zinch.itmo.mts.service.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import me.zinch.itmo.mts.domain.entity.User;
import me.zinch.itmo.mts.repository.UserRepository;
import me.zinch.itmo.mts.web.auth.dto.LoginRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final SessionAuthService sessionAuthService;
    private final TransactionTemplate jtaTransactionTemplate;

    @Override
    public SessionUser login(LoginRequest request, HttpServletRequest servletRequest) {
        User user = jtaTransactionTemplate.execute(status -> {
            User foundUser = userRepository.findByLogin(request.getLogin())
                    .orElseThrow(() -> new UnauthorizedException("Неправильный пароль или логин"));
            if (!passwordService.matches(request.getPassword(), foundUser.getPassword())) {
                throw new UnauthorizedException("Неправильный пароль или логин");
            }
            return foundUser;
        });
        if (user == null) throw new UnauthorizedException("Неправильный пароль или логин");
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
