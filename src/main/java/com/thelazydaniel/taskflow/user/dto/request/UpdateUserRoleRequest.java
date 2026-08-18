package com.thelazydaniel.taskflow.user.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateUserRoleRequest(
        @NotNull(message = "Role is required")
        @Pattern(
                regexp = "^(USER|ADMIN|MANAGER)$",
                message = "ROLE must be USER, ADMIN, or MANAGER"
        )
        String role
){}
