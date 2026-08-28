package com.thelazydaniel.taskflow.security.permission;


import org.springframework.security.core.Authentication;

import java.util.Objects;

public interface EntityPermissionEvaluator {

    String getEntityType();

    boolean supports(Class<?> entityClass);

    boolean hasPermission(Authentication authentication,
                          Object targetDomainObject,
                          String permission);

    boolean hasPermission(Authentication authentication,
                          Long targetId,
                          String permission);

    boolean hasPermission(Authentication authentication, String permission);

    default boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));
    }

    default boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_" + role));
    }



}
