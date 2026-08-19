package com.thelazydaniel.taskflow.project.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thelazydaniel.taskflow.project.enums.ProjectStatus;

import java.time.LocalDate;

public record ProjectSummaryResponse(
        long id,
        String name,
        ProjectStatus status,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate startDate,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate endDate,

        long ownerId
) {
}
