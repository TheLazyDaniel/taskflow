package com.thelazydaniel.taskflow.project.service;

import com.thelazydaniel.taskflow.common.dto.request.PageRequest;
import com.thelazydaniel.taskflow.common.dto.response.PageResponse;
import com.thelazydaniel.taskflow.common.util.SecurityUtils;
import com.thelazydaniel.taskflow.project.ProjectRepository;
import com.thelazydaniel.taskflow.project.dto.ProjectResponseBuilder;
import com.thelazydaniel.taskflow.project.dto.mapper.ProjectMapper;
import com.thelazydaniel.taskflow.project.dto.request.CreateProjectRequest;
import com.thelazydaniel.taskflow.project.dto.request.UpdateProjectRequest;
import com.thelazydaniel.taskflow.project.dto.response.ProjectResponse;
import com.thelazydaniel.taskflow.project.dto.response.ProjectSummaryResponse;
import com.thelazydaniel.taskflow.project.entity.Project;
import com.thelazydaniel.taskflow.project.enums.ProjectStatus;
import com.thelazydaniel.taskflow.project.exception.ProjectArchivedException;
import com.thelazydaniel.taskflow.task.TaskService;
import com.thelazydaniel.taskflow.task.dto.response.TaskSummaryResponse;
import com.thelazydaniel.taskflow.user.service.UserValidationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserValidationService userValidationService;
    private final ProjectValidationService projectValidationService;
    private final ProjectResponseBuilder projectResponseBuilder;
    private final ProjectMapper projectMapper;
    private final TaskService taskService;

    public ProjectService(
            ProjectRepository projectRepository, UserValidationService userValidationService,
            ProjectValidationService projectValidationService, ProjectResponseBuilder projectResponseBuilder,
            ProjectMapper projectMapper,
            TaskService taskService) {
        this.projectRepository = projectRepository;
        this.userValidationService = userValidationService;
        this.projectValidationService = projectValidationService;
        this.projectResponseBuilder = projectResponseBuilder;
        this.projectMapper = projectMapper;
        this.taskService = taskService;
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request){
        request.validateDates();
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Project project = projectMapper.toEntity(request);
        project.setStatus(ProjectStatus.ACTIVE);
        //default
        project.setOwner(userValidationService.validateAndGetUser(currentUserId));
        Project currentProject = projectRepository.save(project);
        return projectResponseBuilder.buildResponse(currentProject);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectSummaryResponse> getAllProjects(PageRequest request){
        Pageable pageable = request.toPageable();
        Page<Project> projects = projectRepository.findAll(pageable);
        return PageResponse.from(projects, projectMapper::toProjectSummaryResponse);
    }

    @Transactional(readOnly = true)
    public ProjectResponse findProjectById(long id){
        Project project = projectValidationService.validateAndGetProject(id);
        return projectResponseBuilder.buildResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(
            UpdateProjectRequest request,
            long id){
        Project project = projectValidationService.validateAndGetProject(id);
        projectValidationService.validateUserAccess(project,"update");
        projectValidationService.validateUpdateAvailability(project);
        projectMapper.updateEntity(request,project);
        Project currentProject = projectRepository.save(project);
        return projectResponseBuilder.buildResponse(currentProject);
    }

    @Transactional
    public ProjectResponse deleteProject(long id){
        Project project = projectValidationService.validateAndGetProject(id);
        projectValidationService.validateUserAccess(project,"delete");
        project.setStatus(ProjectStatus.DELETED);
        Project currentProject = projectRepository.save(project);
        return projectResponseBuilder.buildResponse(currentProject);
    }

    @Transactional
    public ProjectResponse archiveProject(long id){
        Project project = projectValidationService.validateAndGetProject(id);
        if (project.isProjectActive()) {
            project.setStatus(ProjectStatus.ARCHIVED);
        } else {
            throw new ProjectArchivedException("You cannot archive an archived project");
        }
        Project currentProject = projectRepository.save(project);
        return projectResponseBuilder.buildResponse(currentProject);
    }

    @Transactional
    public ProjectResponse restoreProject(long id){
        Project project = projectValidationService.validateAndGetProject(id);
        project.setStatus(ProjectStatus.ACTIVE);
        Project currentProject = projectRepository.save(project);
        return projectResponseBuilder.buildResponse(currentProject);
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskSummaryResponse> findAllRelatedTasks(PageRequest pageRequest, Long id){
        projectValidationService.validateAndGetProject(id);
        return taskService.findTasksByProject(pageRequest,id);
    }


}
