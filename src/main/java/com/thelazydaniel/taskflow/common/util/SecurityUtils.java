package com.thelazydaniel.taskflow.common.util;

import com.thelazydaniel.taskflow.auth.entity.SecurityUser;
import com.thelazydaniel.taskflow.common.exception.IllegalStateException;
import com.thelazydaniel.taskflow.auth.exception.UnauthorizedException;
import com.thelazydaniel.taskflow.user.entity.User;
import com.thelazydaniel.taskflow.user.enums.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null ||  !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authorized");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUser) {
            return ((SecurityUser) principal).getUser();
        } else if (principal instanceof User) {
            return (User) principal;
        } else {
            throw new IllegalStateException("Unexpected user principal type: " + principal.getClass().getName());
        }
    }

    public static long getCurrentUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null ||  !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authorized");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUser) {
            return ((SecurityUser) principal).getUser().getId();
        } else if (principal == null) {
            throw new IllegalStateException("User Principal is null.");
        } else {
            throw new IllegalStateException("Unexpected user principal type: " + principal.getClass().getName());
        }
    }

    public static UserRole getCurrentUserRole(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null ||  !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authorized");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUser) {
            return ((SecurityUser) principal).getUser().getRole();
        } else if (principal instanceof User) {
            return ((User) principal).getRole();
        } else if (principal == null) {
            throw new IllegalStateException("User Principal is null.");
        } else {
            throw new IllegalStateException("Unexpected user principal type: " + principal.getClass().getName());
        }
    }

    public static boolean hasRole(UserRole role) {
        getCurrentUserRole();
        return false;
    }
}
