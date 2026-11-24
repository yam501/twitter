package com.yashin.userservice.exception;

import java.util.Map;

// 404 Not Found
public class NotFoundException extends RuntimeException {

    private final Map<String, Object> details;

    public NotFoundException(String message) {
        this(message, Map.of());
    }

    public NotFoundException(String message, Map<String, Object> details) {
        super(message);
        this.details = details;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
