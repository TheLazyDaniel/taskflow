package com.thelazydaniel.taskflow.task.dto.request;

import com.thelazydaniel.taskflow.task.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateTaskStatusRequest(
        @NotNull(message = "Task status is required")
        @Pattern(
                regexp = "^(TODO|IN_PROGRESS|DONE)$",
                message = "Priority must be TODO, IN_PROGRESS or DONE"
        )
        TaskStatus status
)   {}
