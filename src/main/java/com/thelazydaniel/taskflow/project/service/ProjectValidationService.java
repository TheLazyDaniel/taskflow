package com.thelazydaniel.taskflow.project.service;

import com.thelazydaniel.taskflow.common.util.SecurityUtils;
import com.thelazydaniel.taskflow.project.ProjectRepository;
import com.thelazydaniel.taskflow.project.entity.Project;
import com.thelazydaniel.taskflow.project.enums.ProjectStatus;
import com.thelazydaniel.taskflow.project.exception.*;
import com.thelazydaniel.taskflow.user.entity.User;
import com.thelazydaniel.taskflow.user.enums.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectValidationService {
    private final ProjectRepository projectRepository;

    public ProjectValidationService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Project validateAndGetProject(Long id){
        return projectRepository.findById(id).orElseThrow(
                ()->new ProjectIdNotFoundException(id));
    }

    public void validateUserAccess(Project project, String operation) {
        User currentUser = SecurityUtils.getCurrentUser();

        if (project.getOwner().getId().equals(currentUser.getId()) ||
                currentUser.getRole() == UserRole.ADMIN) {
            return;
        }

        throw new ProjectAccessDeniedException(
                String.format("Only the project owner or admin can %s this project", operation)
        );
    }

    public void validateUpdateAvailability(Project project){
        if (project.getStatus().equals(ProjectStatus.ARCHIVED)){
            throw new ProjectArchivedException("You cannot update an archived project");
        }
        if (project.getStatus().equals(ProjectStatus.DELETED)){
            throw new ProjectDeletedException("You cannot update an deleted project");
        }
    }

    public void validateAddTaskAvailability(Long id) {
        Project project = validateAndGetProject(id);
        if (!project.isProjectActive()) {
            throw new ProjectCannotAcceptTaskException("You cannot add task to an archived/deleted project");
        }
    }
}
