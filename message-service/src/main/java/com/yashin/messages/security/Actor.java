package com.yashin.messages.security;

import com.yashin.messages.api.ApiException;
import java.util.List;

public record Actor(Long id, List<String> roles) {
    public void requireRole(String role) {
        if (!roles.contains(role)) throw new ApiException(403, "Требуется роль " + role);
    }
    public boolean isAdmin() { return roles.contains("administrator"); }
}
