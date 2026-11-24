package com.yashin.userservice.exception;

// 401 Unauthorized: одинаковый ответ и для несуществующего email, и для неверного пароля —
// иначе перебором можно выяснить, какие адреса зарегистрированы (лаба 4)
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
