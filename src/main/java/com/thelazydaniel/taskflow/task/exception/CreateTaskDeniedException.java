package com.thelazydaniel.taskflow.task.exception;

public class CreateTaskDeniedException extends TaskAccessDeniedException {
    public CreateTaskDeniedException(String message) {
        super("CREATE_DENIED: " + message);
    }
}
