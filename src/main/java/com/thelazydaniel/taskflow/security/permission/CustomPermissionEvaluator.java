package com.thelazydaniel.taskflow.security.permission;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import java.io.Serializable;

public class CustomPermissionEvaluator implements PermissionEvaluator {
    @Override
    public boolean hasPermission(@NonNull Authentication authentication, @Nullable Object targetDomainObject, @NonNull Object permission) {
        return false;
    }

    @Override
    public boolean hasPermission(@NonNull Authentication authentication, @NonNull Serializable targetId, @NonNull String targetType, @NonNull Object permission) {
        return false;
    }
}
