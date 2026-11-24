package com.yashin.messages.api;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiError(String message, Map<String, Object> details, OffsetDateTime timestamp) {
    public static ApiError of(String message) {
        return new ApiError(message, Map.of(), OffsetDateTime.now());
    }
}
