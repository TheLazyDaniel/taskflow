package com.thelazydaniel.taskflow.exception;

public class unknownUserRoleException extends RuntimeException {
    public unknownUserRoleException(String role) {
        super("Unknown role: " + role);
    }
}
