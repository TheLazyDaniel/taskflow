package com.thelazydaniel.taskflow.task.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.thelazydaniel.taskflow.task.enums.TaskPriority;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CreateTaskRequest(
        @NotBlank(message = "Title cannot be blank")
        @Size(max = 200, message = "Title cannot have more than 200 characters")
        String title,

        @Size(max = 2000, message = "Description cannot be longer than 2000 characters")
        String description,

        @NotNull(message = "Priority is required")
        TaskPriority priority,


        @JsonFormat(pattern = "yyyy-MM-dd")
        @Future(message = "Due date must be in the future")
        LocalDate dueDate,

        @Min(value = 1, message = "Assignee ID must be a positive number")
        Long assigneeId,

        @NotNull(message = "Project ID is required")
        @Min(value = 1, message = "Project ID must be a positive number")
        Long projectId
) {
}
