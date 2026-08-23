package com.thelazydaniel.taskflow.task.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AssignTaskRequest(
        @NotNull(message = "Assignee ID is required")
        @Min(value = 1, message = "Assignee ID must be a positive number")
        Long assigneeId
) {
}
