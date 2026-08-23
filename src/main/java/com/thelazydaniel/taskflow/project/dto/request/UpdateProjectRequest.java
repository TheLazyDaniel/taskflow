package com.thelazydaniel.taskflow.project.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thelazydaniel.taskflow.common.validation.ValidDateFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProjectRequest(
       @NotBlank
       @Size(min = 1, max = 100)
       String name,

       @Size(max = 500)
       String description,

       @ValidDateFormat(pattern = "yyyy-MM-dd", message = "Invalid end date format. Expected: yyyy-MM-dd")
       @FutureOrPresent(message = "Start date must be in present or the future.")
       @JsonFormat(pattern = "yyyy-MM-dd")
       LocalDate startDate,

       @ValidDateFormat(pattern = "yyyy-MM-dd", message = "Invalid end date format. Expected: yyyy-MM-dd")
       @Future(message = "End date must be in the future")
       @JsonFormat(pattern = "yyyy-MM-dd")
       LocalDate endDate
) {
    public void validateDates() {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be earlier than start date");
        }
    }
}