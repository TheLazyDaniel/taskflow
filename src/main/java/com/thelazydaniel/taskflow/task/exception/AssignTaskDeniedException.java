package com.thelazydaniel.taskflow.task.exception;

public class AssignTaskDeniedException extends TaskAccessDeniedException {
    public AssignTaskDeniedException(String message) {
        super("ASSIGN_DENIED: " +message);
    }
}
