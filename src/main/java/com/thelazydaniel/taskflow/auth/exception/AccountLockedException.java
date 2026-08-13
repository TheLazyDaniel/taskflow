package com.thelazydaniel.taskflow.auth.exception;

public class AccountLockedException extends AuthenticationException{
    private final String username;

    public AccountLockedException(String username) {
        super(String.format("Account locked for %s", username));
        this.username = username;
    }

    public String getUsername() {return username;}
}
