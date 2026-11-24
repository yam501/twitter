package com.yashin.userservice.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignRoleRequest(

        @NotBlank(message = "Имя роли обязательно")
        String roleName
) {
}
