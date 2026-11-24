package com.yashin.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Email не передан")
        @Email(message = "Некорректный email")
        String email,

        @NotBlank(message = "Пароль не передан")
        String password
) {
}
