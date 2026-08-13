package com.thelazydaniel.taskflow.auth.exception;

import lombok.Getter;

@Getter
public class AccountDisabledException extends AuthenticationException {
    private final String username;

    public AccountDisabledException(String username) {
        super(String.format("Account disabled for %s", username));
        this.username = username;
    }

}
