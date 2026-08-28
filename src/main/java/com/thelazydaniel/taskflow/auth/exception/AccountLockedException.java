package com.thelazydaniel.taskflow.auth.exception;

import lombok.Getter;
import org.springframework.security.authentication.LockedException;

@Getter
public class AccountLockedException extends LockedException {
    private final String username;

    public AccountLockedException(String username) {
        super(String.format("Account locked for %s", username));
        this.username = username;
    }
}
