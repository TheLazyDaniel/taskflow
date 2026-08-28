package com.thelazydaniel.taskflow.security.permission;

import com.thelazydaniel.taskflow.security.permission.enums.UserPermission;
import com.thelazydaniel.taskflow.user.UserRepository;
import com.thelazydaniel.taskflow.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class UserPermissionEvaluator implements EntityPermissionEvaluator{

    private final UserRepository userRepository;

    public UserPermissionEvaluator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public String getEntityType() {
        return "USER";
    }

    @Override
    public boolean supports(Class<?> entityClass) {
        return User.class.isAssignableFrom(entityClass);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object target, String permission) {

        if (authentication == null || target == null || permission == null) {
            log.warn("Null parameters in hasPermission");
            return false;
        }

        User targetUser = (User) target;
        String username = authentication.getName();
        UserPermission requiredPermission;

        try {
            requiredPermission = UserPermission.valueOf(permission.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid permission: {}", permission);
            return false;
        }

        log.debug("Checking permission for user: {}, action: {}, target: {}",
                username, permission, targetUser.getUsername());

        if (isAdmin(authentication)){
            log.debug("User {} is ADMIN - granting access", username);
            return true;
        }

        return switch (requiredPermission) {
            case CREATE -> true; //register user
            case READ -> canReadUser(authentication, targetUser);
            case DELETE -> canDeleteUser(authentication, targetUser);
            case UPDATE -> canUpdateUser(authentication, targetUser);
        };
    }


    private boolean canDeleteUser(Authentication authentication, User targetUser) {
        //Maybe more logics
        log.debug("USER and MANAGER not allowed to delete users");
        return false;
    }

    private boolean canUpdateUser(Authentication authentication, User target) {
        String username = authentication.getName();
        boolean isOwnFile = target.getUsername().equalsFoldCase(username);
        if (isOwnFile){
            log.debug("User is updating its own profile");
        } else {
            log.debug("User is not updating its own profile");
        }

        return isOwnFile;
    }

    private boolean canReadUser(Authentication authentication, User target) {
        String username = authentication.getName();
        boolean isOwnFile = target.getUsername().equalsFoldCase(username);
        if (isOwnFile){
            log.debug("User is reading its own profile");
        } else{
            log.debug("User is not reading its own profile");
        }
        return isOwnFile;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Long targetId, String permission) {
        User user = userRepository.findById(targetId).orElse(null);

        if (Objects.isNull(user) && isAdmin(authentication)){
            log.debug("Target User is null. User {} is ADMIN - granting access", authentication.getName());
            return true;
        }

        return user != null && hasPermission(authentication,user,permission);
    }

    @Override
    public boolean hasPermission(Authentication authentication, String permission) {
        //ONLY for CREATE/GET ALL
        if (authentication == null  || permission == null) {
            log.warn("Null parameters in hasPermission");
            return false;
        }

        String username = authentication.getName();
        UserPermission requiredPermission;

        try {
            requiredPermission = UserPermission.valueOf(permission.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid permission: {}", permission);
            return false;
        }

        return switch (requiredPermission){
            case CREATE -> true;
            case READ -> isAdmin(authentication);
            default -> false;
        };
    }


}
