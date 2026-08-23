package com.thelazydaniel.taskflow.auth.dto.response;

public record TokenRefreshResponse(
        String accessToken,
        String refreshToken,
        String tokenType
) {}