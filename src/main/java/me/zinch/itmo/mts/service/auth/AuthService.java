package me.zinch.itmo.mts.service.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import me.zinch.itmo.mts.web.auth.dto.LoginRequest;

public interface AuthService {

    SessionUser login(LoginRequest request, HttpServletRequest servletRequest);

    void logout(HttpSession session);

    SessionUser getCurrentUser(HttpSession session);
}
