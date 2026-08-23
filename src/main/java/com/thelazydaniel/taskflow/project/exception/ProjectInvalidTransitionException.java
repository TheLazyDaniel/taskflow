package com.thelazydaniel.taskflow.project.exception;

public class ProjectInvalidTransitionException extends RuntimeException {
    public ProjectInvalidTransitionException(String message) {
        super(message);
    }
}
