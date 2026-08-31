package com.thelazydaniel.taskflow.security;

import io.jsonwebtoken.Claims;

import java.util.Date;

public record TokenValidationResult(
        boolean isValid,
        boolean isExpired,
        String message,
        Claims claims,
        long remainingExpirationMs
) {
    public static TokenValidationResult valid(Claims claims) {
        long remainingMs = calculateRemainingMs(claims.getExpiration());
        return new TokenValidationResult(true, false, "Token is valid", claims, remainingMs);
    }

    public static TokenValidationResult expired(String message) {
        return new TokenValidationResult(false, true, message, null, 0);
    }

    public static TokenValidationResult invalid(String message) {
        return new TokenValidationResult(false, false, message, null, 0);
    }

    private static long calculateRemainingMs(Date expirationDate) {
        if (expirationDate == null) {
            return 0;
        }
        return Math.max(0, expirationDate.getTime() - System.currentTimeMillis());
    }
}
