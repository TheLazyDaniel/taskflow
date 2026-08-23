package com.thelazydaniel.taskflow.user.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.thelazydaniel.taskflow.user.enums.UserRole;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserPublicSummaryResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        UserRole role,



        //Admin available
        Boolean enabled,
        Boolean accountNonLocked
) implements UserSummaryResponse {
}
