package com.yashin.userservice.dto;

import java.time.OffsetDateTime;

public record LoginResponse(
        Long userId,
        String token,
        OffsetDateTime expiresAt
) {
}
