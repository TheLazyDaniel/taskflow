package com.thelazydaniel.taskflow.task.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.thelazydaniel.taskflow.task.enums.TaskPriority;
import com.thelazydaniel.taskflow.task.enums.TaskStatus;


import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dueDate,

        Long assigneeId,
        Long reporterId,
        Long projectId,

        //for admin
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdDate,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedDate) {
}
