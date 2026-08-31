package com.thelazydaniel.taskflow.auth.dto.request;

import com.thelazydaniel.taskflow.security.TokenType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TokenVerifyRequest(
        @NotBlank(message = "Token is required")
        String token,

        @NotBlank(message = "Token type is required")
        @Pattern(regexp = "(?i)ASSESS|REFRESH", message = "Token type must be ASSESS or REFRESH (case-insensitive)")
        TokenType type
) {}
