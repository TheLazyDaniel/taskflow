package com.thelazydaniel.taskflow.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(min = 3, message = "Username should have at least 3 characters")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username contains invalid characters")
        String username,

        @Email(
                message = "Invalid email format",
                regexp = "^[A-Za-z0-9+_.-]+@(.+)$"
        ) String email,

        String firstName,

        String lastName
) {
}
