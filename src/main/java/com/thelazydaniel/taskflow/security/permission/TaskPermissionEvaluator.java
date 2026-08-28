package com.thelazydaniel.taskflow.security.permission;


import com.thelazydaniel.taskflow.project.ProjectRepository;
import com.thelazydaniel.taskflow.security.permission.enums.TaskPermission;
import com.thelazydaniel.taskflow.task.TaskRepository;
import com.thelazydaniel.taskflow.task.entity.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public class TaskPermissionEvaluator implements EntityPermissionEvaluator{

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    @Override
    public String getEntityType() {
        return "TASK";
    }

    @Override
    public boolean supports(Class<?> entityClass) {
        return Task.class.isAssignableFrom(entityClass);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, String permission) {
        log.debug("Calling Task Evaluator's hasPermission(Authentication authentication, Object targetDomainObject, String permission)");
        if (authentication == null || targetDomainObject == null || permission == null) {
            log.warn("Null parameters in hasPermission");
            return false;
        }

        Task targetTask = (Task) targetDomainObject;
        String username = authentication.getName();

        TaskPermission requiredPermission;
        try {
            requiredPermission = TaskPermission.valueOf(permission.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid permission: {}", permission);
            return false;
        }

        log.debug("Checking permission for user: {}, action: {}, target: {}",
                username, permission, targetTask.getTitle());

        if (isAdmin(authentication)){
            log.debug("User {} is ADMIN - granting access", username);
            return true;
        }

        return switch (requiredPermission) {
            case CREATE -> false; //CREATE no need target
            case READ -> canReadTask(authentication, targetTask);
            case DELETE -> canDeleteTask(authentication, targetTask);
            case UPDATE -> canUpdateTask(authentication, targetTask);
            case UPDATE_STATUS -> canUpdateTaskStatus(authentication, targetTask);
            case ASSIGN -> canAssignTask(authentication, targetTask);
        };

    }

    private boolean canUpdateTask(Authentication authentication, Task targetTask) {
        log.debug("Calling check update task permission method");
        Long currentUserId = PermissionUtils.extractUserId(authentication);
        boolean isManager = hasRole(authentication, "MANAGER");
        boolean isOwner = projectRepository.existsByOwnerIdAndId(currentUserId,targetTask.getProjectId());
        boolean isAssignee = Objects.equals(currentUserId,targetTask.getAssigneeId());
        boolean isReporter = Objects.equals(currentUserId,targetTask.getReporterId());
        if (isManager){
            log.debug("Update Task Granted, User is manager {}",
                    isManager);
        } else if (isOwner){
            log.debug("Update Task Granted, User is owner of project {}", isOwner);
        } else if (isAssignee) {
            log.debug("Update Task Granted, User is assignee of this task {}",
                    isAssignee);
        } else if (isReporter) {
            log.debug("Update Task Granted, User is reporter of this task {}",
                    isReporter);
        }else {
            log.debug("Update Task Not Granted, Manager {}, Owner {}, Assignee {}, Reporter {}"
                    , isManager, isOwner, isAssignee,isReporter);
        }
        return isManager || isOwner || isAssignee || isReporter;
    }

    private boolean canAssignTask(Authentication authentication, Task targetTask) {
        boolean isManager = hasRole(authentication, "MANAGER");

        if (isManager){
            log.debug("Assign Task Granted, User is manager {}",
                    isManager);
        } else  {
            log.debug("Assign Task not Granted, Manager {}",
                    isManager);
        }
        return isManager;
    }

    private boolean canCreateTask(Authentication authentication) {
        log.debug("Calling check create task permission method");
        Long currentUserId = PermissionUtils.extractUserId(authentication);
        boolean isManager = hasRole(authentication, "MANAGER");
        boolean isProjectMember =
                taskRepository.existsByAssigneeIdOrReporterIdAndProjectIdIsNotNull(currentUserId);
        boolean isOwner = projectRepository.existsByOwnerId(Objects.requireNonNull(currentUserId));

        if (isManager){
            log.debug("Create Task Granted, User is manager {}",
                    isManager);
        } else if (isOwner){
            log.debug("Create Task Granted, User is owner of project {}", isOwner);
        } else if (isProjectMember) {
            log.debug("Create Task Granted, User is assignee/reporter of project-related task and task not orphaned {}",
                    isProjectMember);
        } else {
            log.debug("Create Task Not Granted, Manager {}, Owner {}, ProjectMember {}"
                    , isManager, isOwner, isProjectMember);
        }
        return isManager || isOwner || isProjectMember;
    }

    private boolean canReadTask(Authentication authentication, Task targetTask) {
        log.debug("Calling check read task permission method");
        boolean isAssignee = Objects.equals(PermissionUtils.extractUserId(authentication),targetTask.getAssigneeId());
        boolean isReporter = Objects.equals(PermissionUtils.extractUserId(authentication),targetTask.getReporterId());
        if (isAssignee || isReporter){
            log.debug("Read task Granted, User is assignee {}, is reporter {}",isAssignee,isReporter);
        } else {
            log.debug("Read task Not Granted, User is assignee {}, is reporter {}", isAssignee, isReporter);
        }
        return isAssignee || isReporter;
    }

    private boolean canDeleteTask(Authentication authentication, Task targetTask) {
        log.debug("Calling check delete task permission method");
        boolean isManager = hasRole(authentication, "MANAGER");
        if (isManager){
            log.debug("Delete Task Granted, user is manager {}",isManager);
        } else {
            log.debug("Delete Task Not Granted, user is manager {}",isManager);
        }
        return isManager;
    }

    private boolean canUpdateTaskStatus(Authentication authentication, Task targetTask) {
        log.debug("Calling check update task status permission method");
        boolean isAssignee = Objects.equals(PermissionUtils.extractUserId(authentication),targetTask.getAssigneeId());
        if (isAssignee){
            log.debug("Delete Task Granted, user is assignee {}",isAssignee);
        } else {
            log.debug("Delete Task Not Granted, user is assignee {}",isAssignee);
        }
        return isAssignee;
    }


    @Override
    public boolean hasPermission(Authentication authentication, Long targetId, String permission) {
        Task task= taskRepository.findById(targetId).orElse(null);

        if (Objects.isNull(task) && isAdmin(authentication)){
            log.debug("Target Task is null. User {} is ADMIN - granting access", authentication.getName());
            return true;
        }

        return task != null && hasPermission(authentication,task,permission);
    }

    @Override
    public boolean hasPermission(Authentication authentication, String permission) {
        log.debug("Calling Task Evaluator's hasPermission(Authentication authentication, String permission)");
        //ONLY FOR CREATE/GET all
        if (authentication == null || permission == null) {
            log.warn("Null parameters in hasPermission: auth={}, permission={}",
                    authentication, permission);
            return false;
        }

        TaskPermission requiredPermission;
        try {
            requiredPermission = TaskPermission.valueOf(permission.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid permission: {}", permission);
            return false;
        }

        return switch (requiredPermission){
            case CREATE ->  canCreateTask(authentication);
            case READ -> isAdmin(authentication);
            default -> false;
        };
    }
}
