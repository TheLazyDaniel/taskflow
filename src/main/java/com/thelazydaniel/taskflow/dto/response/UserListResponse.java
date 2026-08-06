package com.thelazydaniel.taskflow.dto.response;

import com.thelazydaniel.taskflow.domain.entity.User;
import com.thelazydaniel.taskflow.domain.enums.UserRole;
import tools.jackson.databind.annotation.JsonSerialize;

public record UserListResponse(
        String id,
        String username,
        String email,
        String fullName,
        UserRole role,
        boolean createdDate,
        boolean enabled
) {
    public static UserListResponse from(User user) {
        return new UserListResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName()+user.getLastName(),
                user.getRole(),
                user.isEnabled(),
                user.isEnabled()
        );
    }
}
