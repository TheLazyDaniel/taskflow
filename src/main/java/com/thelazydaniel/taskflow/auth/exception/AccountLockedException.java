package com.thelazydaniel.taskflow.auth.exception;

import lombok.Getter;

@Getter
public class AccountLockedException extends AuthenticationException{
    private final String username;

    public AccountLockedException(String username) {
        super(String.format("Account locked for %s", username));
        this.username = username;
    }
}
