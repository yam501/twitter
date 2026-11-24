package com.yashin.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

        @NotBlank(message = "Имя обязательно")
        @Size(max = 100)
        String firstname,

        @NotBlank(message = "Фамилия обязательна")
        @Size(max = 100)
        String lastname,

        @NotBlank(message = "Email обязателен")
        @Email(message = "Некорректный email")
        @Size(max = 255)
        String email,

        @NotBlank(message = "Пароль обязателен")
        @Size(min = 6, max = 100, message = "Пароль должен быть от 6 до 100 символов")
        String password,

        // необязательное поле; если передано — только цифры с необязательным ведущим +
        @Pattern(regexp = "\\+?[0-9]{6,20}", message = "Некорректный номер телефона")
        String phoneNumber
) {
}
