package com.thelazydaniel.taskflow.task.exception;

public class TaskStatusTransitionDeniedException extends RuntimeException {
    public TaskStatusTransitionDeniedException(String message) {
        super(message);
    }
}
