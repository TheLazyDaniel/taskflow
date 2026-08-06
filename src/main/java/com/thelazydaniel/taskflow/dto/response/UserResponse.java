package com.thelazydaniel.taskflow.dto.response;

import com.thelazydaniel.taskflow.domain.enums.UserRole;

import java.time.LocalDateTime;
import java.util.Optional;

public record UserResponse(
        Long id,
        String username,
        Optional<String> email,
        Optional<String> fullName,
        UserRole role,
        LocalDateTime createdDate
) {

}
