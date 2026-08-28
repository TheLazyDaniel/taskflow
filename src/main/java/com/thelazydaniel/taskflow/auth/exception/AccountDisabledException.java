package com.thelazydaniel.taskflow.auth.exception;

import lombok.Getter;
import org.springframework.security.authentication.DisabledException;

@Getter
public class AccountDisabledException extends DisabledException {
    private final String username;

    public AccountDisabledException(String username) {
        super(String.format("Account disabled for %s", username));
        this.username = username;
    }

}
