package com.thelazydaniel.taskflow.task.exception;

import org.springframework.security.access.AccessDeniedException;

public class TaskAccessDeniedException extends AccessDeniedException {
    public TaskAccessDeniedException(String message) {
        super("TASK_" + message);
    }
}
