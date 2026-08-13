package com.thelazydaniel.taskflow.user.exception;

public class UserIdNotFoundException extends RuntimeException {
    public UserIdNotFoundException(long id) {
        super("User not found with id: " + id);
    }
}


