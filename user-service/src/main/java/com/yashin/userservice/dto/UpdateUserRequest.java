package com.yashin.userservice.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// все поля необязательны, обновляются только переданные;
// email и password через этот метод не меняются (нужны отдельные процедуры с подтверждением)
public record UpdateUserRequest(

        @Size(max = 100)
        String firstname,

        @Size(max = 100)
        String lastname,

        @Pattern(regexp = "\\+?[0-9]{6,20}", message = "Некорректный номер телефона")
        String phoneNumber
) {
}
