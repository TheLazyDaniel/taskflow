package com.thelazydaniel.taskflow.project.exception;

public class ProjectIdNotFoundException extends RuntimeException {
    public ProjectIdNotFoundException(Long id) {
        super("Project not found with id: " + id);
    }
}
