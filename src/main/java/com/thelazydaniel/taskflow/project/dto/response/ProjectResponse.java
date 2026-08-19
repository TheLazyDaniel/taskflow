package com.thelazydaniel.taskflow.project.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thelazydaniel.taskflow.project.enums.ProjectStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProjectResponse(
        long id,
        String name,
        String description,
        ProjectStatus status,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate startDate,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate endDate,

        long ownerId,
        String ownerName,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdDate,

        //For admin
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedDate
) {
}
