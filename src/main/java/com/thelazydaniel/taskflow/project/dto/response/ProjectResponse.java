package com.thelazydaniel.taskflow.project.dto.response;


import com.thelazydaniel.taskflow.project.enums.ProjectStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;


public interface ProjectResponse {
    Long id();
    String name();
    String description();
    ProjectStatus status();

    LocalDate startDate();

    LocalDate endDate();

    LocalDateTime createdDate();

    LocalDateTime updatedDate();
}
