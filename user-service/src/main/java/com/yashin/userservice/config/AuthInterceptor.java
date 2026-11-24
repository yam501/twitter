package com.yashin.userservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yashin.userservice.dto.ApiError;
import com.yashin.userservice.util.AuthContext;
import com.yashin.userservice.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

// проверяет Bearer-токен на защищённых эндпоинтах и кладёт userId/roles в запрос.
// Токен проверяется локально (подпись JWT), похода в другой сервис нет (лаба 5)
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        boolean required = isProtected(request.getMethod(), request.getRequestURI());

        if (header == null || !header.startsWith("Bearer ")) {
            if (required) {
                return unauthorized(response, "Требуется авторизация");
            }
            return true; // публичный эндпоинт, токена нет — идём дальше анонимно
        }

        try {
            String token = header.substring("Bearer ".length());
            request.setAttribute(AuthContext.USER_ID_ATTR, jwtUtil.parseUserId(token));
            request.setAttribute(AuthContext.ROLES_ATTR, jwtUtil.parseRoles(token));
        } catch (JwtException | IllegalArgumentException e) {
            // недействительный токен блокирует только защищённые эндпоинты
            if (required) {
                return unauthorized(response, "Токен недействителен или истёк");
            }
        }
        return true;
    }

    // защищённые эндпоинты: PATCH профиля и операции с ролями.
    // Регистрация, вход, чтение пользователя и батч-выборка — публичные (лаба 4)
    private boolean isProtected(String method, String uri) {
        if ("PUT".equals(method) && uri.matches("/api/v1/users/[^/]+/roles/[^/]+")) {
            return true;
        }
        if ("PATCH".equals(method) && uri.matches("/api/v1/users/[^/]+")) {
            return true;
        }
        if ("POST".equals(method) && uri.matches("/api/v1/users/[^/]+/roles")) {
            return true;
        }
        return "DELETE".equals(method) && uri.matches("/api/v1/users/[^/]+/roles/[^/]+");
    }

    private boolean unauthorized(HttpServletResponse response, String message) throws java.io.IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ApiError.of(message));
        return false;
    }
}
