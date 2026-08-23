package com.thelazydaniel.taskflow.project.dto;

import com.thelazydaniel.taskflow.common.util.SecurityUtils;
import com.thelazydaniel.taskflow.project.dto.response.ProjectAdminResponse;
import com.thelazydaniel.taskflow.project.dto.response.ProjectManagerResponse;
import com.thelazydaniel.taskflow.project.dto.response.ProjectPublicResponse;
import com.thelazydaniel.taskflow.project.dto.response.ProjectResponse;
import com.thelazydaniel.taskflow.project.entity.Project;
import com.thelazydaniel.taskflow.user.dto.response.UserAdminResponse;
import com.thelazydaniel.taskflow.user.dto.response.UserManagerResponse;
import com.thelazydaniel.taskflow.user.dto.response.UserPublicResponse;
import com.thelazydaniel.taskflow.user.dto.response.UserResponse;
import com.thelazydaniel.taskflow.user.enums.UserRole;
import com.thelazydaniel.taskflow.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectResponseBuilder {

    private final UserService userService;

    public ProjectResponse buildResponse(Project project) {
        if (project == null) return null;

        UserRole currentRole = SecurityUtils.getCurrentUserRole();
        UserResponse owner = userService.findUserById(project.getOwnerId());

        return switch (currentRole) {
            case ADMIN -> buildAdminResponse(project, (UserAdminResponse) owner);
            case MANAGER -> buildManagerResponse(project, (UserManagerResponse) owner);
            default -> buildPublicResponse(project, (UserPublicResponse) owner);
        };
    }


    private ProjectAdminResponse buildAdminResponse(Project project, UserAdminResponse owner) {
        return new ProjectAdminResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getStartDate(),
                project.getEndDate(),
                owner.id(),
                owner.username(),
                owner.email(),
                owner.role().name(),
                owner.firstName(),
                owner.lastName(),
                owner.enabled().toString(),
                owner.accountNonLocked().toString(),
                project.getCreatedDate(),
                project.getUpdatedDate()
        );
    }

    private ProjectManagerResponse buildManagerResponse(Project project, UserManagerResponse owner) {
        return new ProjectManagerResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getStartDate(),
                project.getEndDate(),
                owner.id(),
                owner.username(),
                owner.email(),
                owner.role().name(),
                owner.firstName(),
                owner.lastName(),
                project.getCreatedDate(),
                project.getUpdatedDate()
        );
    }

    private ProjectPublicResponse buildPublicResponse(Project project, UserPublicResponse owner) {
        return new ProjectPublicResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getStartDate(),
                project.getEndDate(),
                owner.id(),
                owner.username(),
                project.getCreatedDate(),
                project.getUpdatedDate()
        );
    }
}


