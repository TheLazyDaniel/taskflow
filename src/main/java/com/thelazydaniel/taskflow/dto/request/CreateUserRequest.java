package com.thelazydaniel.taskflow.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, message = "Username should have at least 3 characters")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username contains invalid characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(
                message = "Invalid email format",
                regexp = "^[A-Za-z0-9+_.-]+@(.+)$"
        ) String email,

        @NotBlank(message = "Password is required")
        @Size(min = 3, message = "Password should have at least 3 characters")
        String password,

        String firstName,

        String lastName
){}

