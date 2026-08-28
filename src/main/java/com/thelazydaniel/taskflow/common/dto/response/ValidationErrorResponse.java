package com.thelazydaniel.taskflow.common.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ValidationErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        Map<String, List<String>> fieldErrors,
        List<String> globalErrors,
        String path
) {
}