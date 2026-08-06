package com.thelazydaniel.taskflow.dto.request;

import com.thelazydaniel.taskflow.domain.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateUserRoleRequest(
        @NotNull(message = "Role is required")
        @Pattern(
                regexp = "^(USER|ADMIN|MANAGER)$",
                message = "ROLE must be USER, ADMIN, or MANAGER"
        )
        String Role
){}
