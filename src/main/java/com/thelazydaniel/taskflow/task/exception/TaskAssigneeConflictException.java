package com.thelazydaniel.taskflow.task.exception;

public class TaskAssigneeConflictException extends RuntimeException {
    public TaskAssigneeConflictException(String message) {
        super(message);
    }
}
