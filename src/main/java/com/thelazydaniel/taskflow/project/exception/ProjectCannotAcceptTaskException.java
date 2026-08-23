package com.thelazydaniel.taskflow.project.exception;

public class ProjectCannotAcceptTaskException extends RuntimeException {
    public ProjectCannotAcceptTaskException(String message) {
        super(message);
    }
}
