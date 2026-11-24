package com.yashin.userservice.util;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

// доступ к данным авторизации, которые AuthInterceptor положил в запрос
public final class AuthContext {

    public static final String USER_ID_ATTR = "authUserId";
    public static final String ROLES_ATTR = "authRoles";

    private AuthContext() {
    }

    public static Long userId(HttpServletRequest request) {
        return (Long) request.getAttribute(USER_ID_ATTR);
    }

    @SuppressWarnings("unchecked")
    public static List<String> roles(HttpServletRequest request) {
        return (List<String>) request.getAttribute(ROLES_ATTR);
    }

    public static boolean isAdmin(HttpServletRequest request) {
        List<String> roles = roles(request);
        return roles != null && roles.contains("administrator");
    }
}
