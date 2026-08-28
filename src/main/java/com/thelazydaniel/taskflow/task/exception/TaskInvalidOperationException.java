package com.thelazydaniel.taskflow.task.exception;

public class TaskInvalidOperationException extends RuntimeException {
    public TaskInvalidOperationException(String message) {
        super(message);
    }
}
