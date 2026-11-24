package com.yashin.userservice.dto;

import java.time.OffsetDateTime;
import java.util.Map;

// единый формат ошибок из API-контрактов (лаба 4)
public record ApiError(
        String message,
        Map<String, Object> details,
        OffsetDateTime timestamp
) {

    public static ApiError of(String message) {
        return new ApiError(message, Map.of(), OffsetDateTime.now());
    }

    public static ApiError of(String message, Map<String, Object> details) {
        return new ApiError(message, details == null ? Map.of() : details, OffsetDateTime.now());
    }
}
