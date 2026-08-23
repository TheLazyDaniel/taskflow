package com.thelazydaniel.taskflow.task.exception;

public class TaskIdNotFoundException extends RuntimeException {
    public TaskIdNotFoundException(long id) {
        String message = "Task not found with id: " + id;
        super(message);
    }
}
