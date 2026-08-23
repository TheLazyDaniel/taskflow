package com.thelazydaniel.taskflow.task.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thelazydaniel.taskflow.common.validation.ValidDateFormat;
import com.thelazydaniel.taskflow.task.enums.TaskPriority;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UpdateTaskRequest(
        @Size(max = 200, message = "Title cannot be longer than 200 characters")
        String title,

        @Size(max = 2000, message = "Description cannot be longer than 2000 characters")
        String description,

        @Pattern(
                regexp = "^(LOW|MEDIUM|HIGH|CRITICAL)$",
                message = "Priority must be LOW, MEDIUM, HIGH or CRITICAL"
        )
        TaskPriority priority,

        @ValidDateFormat(pattern = "yyyy-MM-dd", message = "Invalid end date format. Expected: yyyy-MM-dd")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Future(message = "Due date must be in the future")
        LocalDate dueDate
) {
}
