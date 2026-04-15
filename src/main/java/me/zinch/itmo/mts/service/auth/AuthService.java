package me.zinch.itmo.mts.service.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import me.zinch.itmo.mts.web.auth.dto.LoginRequest;
import me.zinch.itmo.mts.web.auth.dto.RegisterManagerRequest;

public interface AuthService {

    SessionUser registerManager(RegisterManagerRequest request, HttpServletRequest servletRequest);

    SessionUser login(LoginRequest request, HttpServletRequest servletRequest);

    void logout(HttpSession session);

    SessionUser getCurrentUser(HttpSession session);
}
