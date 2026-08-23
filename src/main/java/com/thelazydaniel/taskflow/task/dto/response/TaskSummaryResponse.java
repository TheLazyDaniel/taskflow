package com.thelazydaniel.taskflow.task.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.thelazydaniel.taskflow.task.enums.TaskPriority;
import com.thelazydaniel.taskflow.task.enums.TaskStatus;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskSummaryResponse(
      long id,
      String title,
      TaskStatus status,
      TaskPriority priority,

      @JsonFormat(pattern = "yyyy-MM-dd")
      LocalDate dueDate,

      long assigneeId,
      long reporterId,
      long projectId
) {
}