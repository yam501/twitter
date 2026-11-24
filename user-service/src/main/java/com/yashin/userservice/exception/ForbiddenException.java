package com.yashin.userservice.exception;

// 403 Forbidden
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
