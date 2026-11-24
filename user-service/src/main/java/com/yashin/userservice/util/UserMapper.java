package com.yashin.userservice.util;

import com.yashin.userservice.dto.UserResponse;
import com.yashin.userservice.entity.Role;
import com.yashin.userservice.entity.User;

import java.util.List;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .sorted()
                .toList();
        return new UserResponse(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getPhoneNumber(),
                roleNames,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
