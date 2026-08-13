package com.thelazydaniel.taskflow.user.exception;

public class UserNameNotFoundException extends RuntimeException {
    public UserNameNotFoundException(String username) {
        super("User not found with username: " + username);
    }
}


