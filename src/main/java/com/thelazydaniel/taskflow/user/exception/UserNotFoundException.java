package com.thelazydaniel.taskflow.user.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(long id) {
        super("User not found with id: " + id);
    }
}


