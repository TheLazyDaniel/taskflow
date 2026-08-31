package com.thelazydaniel.taskflow.auth.exception;

public class LogoutException extends RuntimeException {
    public LogoutException(String message) {
        super(message);
    }

    public LogoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
