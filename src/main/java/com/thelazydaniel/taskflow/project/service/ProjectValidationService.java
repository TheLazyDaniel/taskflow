package com.thelazydaniel.taskflow.project.service;

import com.thelazydaniel.taskflow.common.exception.IllegalArgumentException;
import com.thelazydaniel.taskflow.common.util.SecurityUtils;
import com.thelazydaniel.taskflow.project.ProjectRepository;
import com.thelazydaniel.taskflow.project.entity.Project;
import com.thelazydaniel.taskflow.project.enums.ProjectStatus;
import com.thelazydaniel.taskflow.project.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@Slf4j
public class ProjectValidationService {
    private final ProjectRepository projectRepository;

    public ProjectValidationService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Long validateAndGetProjectId(Long id){
        log.debug("Validating project: projectId={}", id);
        projectRepository.findById(id).orElseThrow(
                ()->new ProjectIdNotFoundException(id));
        return id;
    }

    public Project validateAndGetProject(Long id){
        log.debug("Validating project: projectId={}", id);
        return projectRepository.findById(id).orElseThrow(
                ()->new ProjectIdNotFoundException(id));
    }


    public void validateProjectNameExistenceInUser(String projectName){
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (projectRepository.existsByNameAndOwnerId(projectName,currentUserId)){
            log.warn("Project name {} already exists for user with id {}",projectName, currentUserId);
            throw new IllegalArgumentException(String.format("Project with name: %s already exists",projectName));
        }
    }

    public void validateUpdateAvailability(Project project){
        if (project.getStatus().equals(ProjectStatus.ARCHIVED)){
            throw new ProjectOperationBlockedException("You cannot update an archived project");
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

    public boolean validateAddTaskToAccessibleProject(Long projectId){
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return projectRepository.existsByOwnerIdAndId(currentUserId,projectId);
    }

    public Long getCurrentUserId(){
        return SecurityUtils.getCurrentUserId();
    }

    public boolean isOwnerOfProject(Long projectId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return projectRepository.existsByOwnerIdAndId(currentUserId,projectId);
    }
}
