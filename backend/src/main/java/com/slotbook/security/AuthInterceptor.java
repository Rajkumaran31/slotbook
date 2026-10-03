package com.slotbook.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// Protects /api/bookings/** : reads "Authorization: Bearer <jwt>" and stores userId on the request
@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final JwtUtil jwt;
    public AuthInterceptor(JwtUtil jwt) { this.jwt = jwt; }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        if (req.getMethod().equals("OPTIONS")) return true;
        String h = req.getHeader("Authorization");
        try {
            req.setAttribute("userId", jwt.parse(h.substring(7)));
            return true;
        } catch (Exception e) {
            res.sendError(401, "Please log in again");
            return false;
        }
    }
}
