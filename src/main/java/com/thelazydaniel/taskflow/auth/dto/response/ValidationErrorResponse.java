package com.thelazydaniel.taskflow.auth.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ValidationErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        Map<String,String> fieldErrors,
        List<String> globalErrors,
        String path
) {
}