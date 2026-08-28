package com.thelazydaniel.taskflow.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TokenVerifyResponse(
        boolean valid,
        String username,
        String message,
        Long expiresIn, // seconds until expiry
        String tokenType
) {}
