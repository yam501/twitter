package com.yashin.userservice.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record UserResponse(
        Long id,
        String firstname,
        String lastname,
        String email,
        String phoneNumber,
        List<String> roles,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
