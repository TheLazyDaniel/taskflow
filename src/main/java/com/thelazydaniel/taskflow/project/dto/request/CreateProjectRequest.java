package com.thelazydaniel.taskflow.project.dto.request;


import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateProjectRequest(
        @NotBlank(message = "Project name cannot be blank.")
        @Size(min = 1, max = 100, message = "Project name length must be between 1 and 100.")
        String name,

        @Size(max = 500, message = "Project description length is at most 500")
        String description,

        @FutureOrPresent(message = "Start date must be in present or the future.")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate startDate,

        @Future(message = "End date must be in the future.")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate endDate


) {
    public void validateDates() {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be earlier than start date");
        }
    }
}
