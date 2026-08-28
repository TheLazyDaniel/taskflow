package com.thelazydaniel.taskflow.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TokenVerifyRequest(
        @NotBlank(message = "Token is required")
        String token
) {}
