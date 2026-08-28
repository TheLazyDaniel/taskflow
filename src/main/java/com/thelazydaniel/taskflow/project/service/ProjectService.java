package com.thelazydaniel.taskflow.project.service;

import com.thelazydaniel.taskflow.common.dto.request.PageRequest;
import com.thelazydaniel.taskflow.common.dto.response.PageResponse;
import com.thelazydaniel.taskflow.project.ProjectRepository;
import com.thelazydaniel.taskflow.project.dto.ProjectResponseBuilder;
import com.thelazydaniel.taskflow.project.dto.mapper.ProjectMapper;
import com.thelazydaniel.taskflow.project.dto.request.CreateProjectRequest;
import com.thelazydaniel.taskflow.project.dto.request.UpdateProjectRequest;
import com.thelazydaniel.taskflow.project.dto.response.ProjectResponse;
import com.thelazydaniel.taskflow.project.dto.response.ProjectSummaryResponse;
import com.thelazydaniel.taskflow.project.entity.Project;
import com.thelazydaniel.taskflow.project.enums.ProjectStatus;
import com.thelazydaniel.taskflow.project.exception.ProjectOperationBlockedException;
import com.thelazydaniel.taskflow.project.exception.ProjectCannotUpdateException;
import com.thelazydaniel.taskflow.task.service.TaskService;
import com.thelazydaniel.taskflow.task.dto.response.TaskSummaryResponse;
import com.thelazydaniel.taskflow.task.service.TaskValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectValidationService projectValidationService;
    private final ProjectResponseBuilder projectResponseBuilder;
    private final ProjectMapper projectMapper;
    private final TaskService taskService;
    private final TaskValidationService taskValidationService;


    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request){
        log.info("Creating project");
        request.validateDates();
        projectValidationService.validateProjectNameExistenceInUser(request.name());
        Project project = projectMapper.toEntity(request);
        project.setStatus(ProjectStatus.ACTIVE);
        //default
        Long currentUserId = projectValidationService.getCurrentUserId();
        project.setOwnerId(currentUserId);
        Project currentProject = projectRepository.save(project);
        log.info("Project created: projectId={}, ownerId={}", currentProject.getId(), currentUserId);
        return projectResponseBuilder.buildResponse(currentProject);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectSummaryResponse> getAllProjects(PageRequest request){
        log.debug("Finding all projects: page={}, size={}", request.page(), request.size());
        Pageable pageable = request.toPageable();
        Page<Project> projects = projectRepository.findAll(pageable);
        return PageResponse.from(projects, projectMapper::toProjectSummaryResponse);
    }

    @Transactional(readOnly = true)
    public ProjectResponse findProjectById(long id){
        log.debug("Finding project: id={}", id);
        Project project = projectValidationService.validateAndGetProject(id);
        return projectResponseBuilder.buildResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(
            UpdateProjectRequest request,
            long id){
        log.info("Updating project: id={}", id);
        Project project = projectValidationService.validateAndGetProject(id);
        projectValidationService.validateUpdateAvailability(project);
        if (taskValidationService.isProgressingTasksUnderProject(project.getId())){
            throw new ProjectCannotUpdateException("You cannot update a project with tasks with status IN_PROGRESS");
        }
        projectValidationService.validateProjectNameExistenceInUser(request.name());
        projectMapper.updateEntity(request,project);
        Project currentProject = projectRepository.save(project);
        log.info("Project updated: projectId={}", id);
        return projectResponseBuilder.buildResponse(currentProject);
    }

    @Transactional
    public ProjectResponse deleteProject(long id){
        log.info("Deleting project: id={}", id);
        Project project = projectValidationService.validateAndGetProject(id);
        if (taskValidationService.isActiveTasksUnderProject(id)) {
            throw new ProjectOperationBlockedException("The project still has active tasks. You cannot delete it");
        }
        project.setStatus(ProjectStatus.DELETED);
        Project currentProject = projectRepository.save(project);
        log.info("Detaching Tasks by project id: project id={}", id);
        taskService.DetachTasksByProjectId(id);
        return projectResponseBuilder.buildResponse(currentProject);
    }

    @Transactional
    public ProjectResponse archiveProject(long id){
        log.info("Archiving project: id={}", id);
        Project project = projectValidationService.validateAndGetProject(id);

        if (taskValidationService.isActiveTasksUnderProject(id)){
            throw new ProjectOperationBlockedException("The project still has active tasks. You cannot archive it");
        } else if (project.isProjectActive()) {
            project.setStatus(ProjectStatus.ARCHIVED);
        } else {
            throw new ProjectOperationBlockedException("You cannot archive an archived project");
        }
        Project currentProject = projectRepository.save(project);
        return projectResponseBuilder.buildResponse(currentProject);
    }

    @Transactional
    public ProjectResponse restoreProject(long id){
        log.info("Restoring project: id={}", id);
        Project project = projectValidationService.validateAndGetProject(id);
        project.setStatus(ProjectStatus.ACTIVE);
        Project currentProject = projectRepository.save(project);
        return projectResponseBuilder.buildResponse(currentProject);
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskSummaryResponse> findAllRelatedTasks(PageRequest pageRequest, Long id){
        log.debug("Finding related tasks: projectId={}, page={}, size={}",
                id, pageRequest.page(), pageRequest.size());
        projectValidationService.validateAndGetProject(id);
        return taskService.findTasksByProject(pageRequest,id);
    }


}
