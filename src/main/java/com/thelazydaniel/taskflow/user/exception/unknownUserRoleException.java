package com.thelazydaniel.taskflow.user.exception;

public class unknownUserRoleException extends RuntimeException {
    public unknownUserRoleException(String role) {
        super("Unknown role: " + role);
    }
}
