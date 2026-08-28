package com.thelazydaniel.taskflow.security.permission;

import com.thelazydaniel.taskflow.auth.entity.SecurityUser;
import com.thelazydaniel.taskflow.user.entity.User;
import org.springframework.security.core.Authentication;

public class PermissionUtils {

    public static User extractUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUser securityUser) {
            return securityUser.getUser();
        } else if (principal instanceof User user) {
            return user;
        }
        return null;
    }

    public static Long extractUserId(Authentication authentication) {
        User user = extractUser(authentication);
        return user != null ? user.getId() : null;
    }

}