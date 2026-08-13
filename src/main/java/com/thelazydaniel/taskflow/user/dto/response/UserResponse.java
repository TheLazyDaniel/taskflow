package com.thelazydaniel.taskflow.user.dto.response;

import com.thelazydaniel.taskflow.user.enums.UserRole;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        LocalDateTime createdDate
) {

}
