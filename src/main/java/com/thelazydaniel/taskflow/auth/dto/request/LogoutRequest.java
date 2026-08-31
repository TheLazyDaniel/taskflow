package com.thelazydaniel.taskflow.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank(message = "Refresh Token cannot be blank")
        String refreshToken
) {
}
