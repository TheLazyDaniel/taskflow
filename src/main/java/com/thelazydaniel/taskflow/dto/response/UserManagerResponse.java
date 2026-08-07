package com.thelazydaniel.taskflow.dto.response;

import com.thelazydaniel.taskflow.domain.enums.UserRole;

import java.time.LocalDateTime;

public record UserManagerResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        LocalDateTime createdDate,
        LocalDateTime updatedDate,
        LocalDateTime lastLoginDate
) {
}
