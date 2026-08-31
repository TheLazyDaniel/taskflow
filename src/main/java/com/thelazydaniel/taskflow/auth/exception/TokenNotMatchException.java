package com.thelazydaniel.taskflow.auth.exception;

public class TokenNotMatchException extends RuntimeException {
    public TokenNotMatchException(String message) {
        super(message);
    }
}
