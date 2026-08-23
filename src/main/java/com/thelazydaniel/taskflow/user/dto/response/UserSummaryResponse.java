package com.thelazydaniel.taskflow.user.dto.response;

import com.thelazydaniel.taskflow.user.enums.UserRole;

public interface UserSummaryResponse {
    Long id();
    String username();
    String firstName();
    String lastName();
    UserRole role();
}
