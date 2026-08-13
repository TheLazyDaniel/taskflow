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

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null ||  !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authorized");
        }
        return authentication.getName();
    }

    public static long getCurrentUserId(){
        return getCurrentUser().getId();
    }

    public static String getCurrentUserRole(){
        return String.valueOf(getCurrentUser().getRole());
    }

    public static boolean hasRole(UserRole role) {
        return role == getCurrentUser().getRole();
    }
}
