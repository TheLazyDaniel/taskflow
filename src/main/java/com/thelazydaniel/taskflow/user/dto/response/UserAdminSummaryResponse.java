package com.thelazydaniel.taskflow.user.dto.response;

import com.thelazydaniel.taskflow.user.enums.UserRole;

public record UserAdminSummaryResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        UserRole role,

        Boolean enabled,
        Boolean accountNonLocked
) implements UserSummaryResponse {
}
