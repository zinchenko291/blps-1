package me.zinch.itmo.mts.service.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import me.zinch.itmo.mts.domain.entity.User;
import me.zinch.itmo.mts.domain.enums.UserRole;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SessionAuthService {

    private static final String USER_ID = "auth.user.id";
    private static final String USER_LOGIN = "auth.user.login";
    private static final String USER_NAME = "auth.user.name";
    private static final String USER_ROLE = "auth.user.role";

    public SessionUser authenticate(HttpServletRequest request, User user) {
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession session = request.getSession(true);
        session.setAttribute(USER_ID, user.getId().toString());
        session.setAttribute(USER_LOGIN, user.getLogin());
        session.setAttribute(USER_NAME, user.getName());
        session.setAttribute(USER_ROLE, user.getRole().name());

        return toSessionUser(session);
    }

    public void logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }

    public SessionUser requireAuthenticated(HttpSession session) {
        if (session == null) {
            throw new UnauthorizedException("Требуется авторизация");
        }
        return toSessionUser(session);
    }

    private SessionUser toSessionUser(HttpSession session) {
        Object id = session.getAttribute(USER_ID);
        Object login = session.getAttribute(USER_LOGIN);
        Object name = session.getAttribute(USER_NAME);
        Object role = session.getAttribute(USER_ROLE);

        if (!(id instanceof String idValue) || !(login instanceof String loginValue)
                || !(name instanceof String nameValue) || !(role instanceof String roleValue)) {
            throw new UnauthorizedException("Некорректная сессия");
        }

        return new SessionUser(
                UUID.fromString(idValue),
                loginValue,
                nameValue,
                UserRole.valueOf(roleValue)
        );
    }
}
