package com.thelazydaniel.taskflow.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.Date;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TokenVerifyResponse(
        boolean valid,
        boolean expired,
        boolean revoked,
        String message,
        String tokenType,
        String username,
        List<String> roles,
        long remainingExpirationMs,
        @JsonFormat(pattern = "yyyy-MM-dd")
        Date expirationDate
){
}
