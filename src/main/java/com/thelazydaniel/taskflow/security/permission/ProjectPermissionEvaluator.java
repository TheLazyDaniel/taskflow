package com.thelazydaniel.taskflow.security.permission;


import com.thelazydaniel.taskflow.project.ProjectRepository;
import com.thelazydaniel.taskflow.project.entity.Project;
import com.thelazydaniel.taskflow.security.permission.enums.ProjectPermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProjectPermissionEvaluator implements EntityPermissionEvaluator{

    private final ProjectRepository projectRepository;

    @Override
    public String getEntityType() {
        return "PROJECT";
    }

    @Override
    public boolean supports(Class<?> entityClass) {
        return Project.class.isAssignableFrom(entityClass);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object target, String permission) {


        if (authentication == null || target == null || permission == null) {
            log.warn("Null parameters in hasPermission: auth={}, target={}, permission={}",
                    authentication, target, permission);
            return false;
        }

        if (!(target instanceof Project targetProject)) {
            log.warn("Target is not a Project: {}", target.getClass().getName());
            return false;
        }

        String username = authentication.getName();
        ProjectPermission requiredPermission;

        try {
            requiredPermission = ProjectPermission.valueOf(permission.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid permission: {}", permission);
            return false;
        }

        log.debug("Checking permission for user: {}, action: {}, target: {}",
                username, permission, targetProject.getName());

        // Admin bypass
        if (isAdmin(authentication)) {
            log.debug("User {} is ADMIN - granting access", username);
            return true;
        }

        return switch (requiredPermission) {
            case CREATE -> false; //create require no target
            case READ -> canReadProject(authentication, targetProject);
            case UPDATE -> canUpdateProject(authentication, targetProject);
            case DELETE -> canDeleteProject(authentication, targetProject);
        };
    }

    @Override
    public boolean hasPermission(Authentication authentication, Long targetId, String permission) {
        if (authentication == null || targetId == null || permission == null) {
            log.warn("Null parameters in hasPermission: auth={}, targetId={}, permission={}",
                    authentication, targetId, permission);
            return false;
        }

        Project project = projectRepository.findById(targetId).orElse(null);

        if (project == null) {
            log.debug("Project not found with ID: {}", targetId);
            return isAdmin(authentication);
            //Only let ADMIN access non-existent resources for testing behavior
        }

        return hasPermission(authentication,project,permission);
    }


    @Override
    public boolean hasPermission(Authentication authentication, String permission) {
        log.info("🔍 ProjectPermissionEvaluator.hasPermission(Authentication, String) called: auth={}, permission={}",
                authentication != null ? authentication.getName() : "null",
                permission);
        //ONLY FOR CREATE/GET all
        if (authentication == null || permission == null) {
            log.warn("Null parameters in hasPermission: auth={}, permission={}",
                    authentication, permission);
            return false;
        }

        ProjectPermission requiredPermission;
        try {
            requiredPermission = ProjectPermission.valueOf(permission.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid permission: {}", permission);
            return false;
        }

        return switch (requiredPermission){
            case CREATE -> canCreateProject(authentication);
            case READ -> isAdmin(authentication);
            default -> false;
        };
    }

    boolean canReadProject(Authentication authentication, Project project){
        boolean isOwnProject = Objects.equals(PermissionUtils.extractUserId(authentication),project.getOwnerId());
        if (isOwnProject){
            log.debug("User is reading own project, READ Project Granted");
        } else {
            log.debug("User is not reading own project, READ Project NOT Granted");
        }
        return isOwnProject;
    }

    boolean canCreateProject(Authentication authentication){
        boolean isAuthenticated = authentication.isAuthenticated();
        if (isAuthenticated){
            log.debug("User is authenticated , CREATE Project Granted");
        } else {
            log.debug("User is not authenticated, CREATE Project NOT Granted");
        }
        return isAuthenticated;
    }

    boolean canDeleteProject(Authentication authentication, Project project){
        //Require both MANAGER and owner to delete
        boolean isManager = hasRole(authentication,"MANAGER");
        boolean isOwnProject = Objects.equals(PermissionUtils.extractUserId(authentication),project.getOwnerId());
        if (isManager && isOwnProject){
            log.debug("User is MANAGER and deleting own project, DELETE Project Granted");
        } else {
            log.debug("User is not MANAGER {} or now deleting own project {}, DELETE Project NOT Granted",isManager,isOwnProject);
        }
        return isManager && isOwnProject;
    }

    boolean canUpdateProject(Authentication authentication, Project project){
        boolean isOwnProject = Objects.equals(PermissionUtils.extractUserId(authentication),project.getOwnerId());
        if (isOwnProject){
            log.debug("User is updating own project, UPDATE Project Granted");
        } else {
            log.debug("User is not updating own project, UPDATE Project NOT Granted");
        }
        return isOwnProject;
    }

}
