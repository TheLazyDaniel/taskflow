package com.thelazydaniel.taskflow.task.exception;

public class TaskFieldUpdateDeniedException extends TaskAccessDeniedException {
    public TaskFieldUpdateDeniedException(String message) {
        super("FIELD_UPDATE_DENIED: " + message);
    }
}
