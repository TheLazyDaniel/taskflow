package com.thelazydaniel.taskflow.project;

import com.thelazydaniel.taskflow.common.dto.request.PageRequest;
import com.thelazydaniel.taskflow.common.dto.response.PageResponse;
import com.thelazydaniel.taskflow.common.util.SecurityUtils;
import com.thelazydaniel.taskflow.project.dto.mapper.ProjectMapper;
import com.thelazydaniel.taskflow.project.dto.request.CreateProjectRequest;
import com.thelazydaniel.taskflow.project.dto.request.UpdateProjectRequest;
import com.thelazydaniel.taskflow.project.dto.response.ProjectResponse;
import com.thelazydaniel.taskflow.project.dto.response.ProjectSummaryResponse;
import com.thelazydaniel.taskflow.project.entity.Project;
import com.thelazydaniel.taskflow.project.enums.ProjectStatus;
import com.thelazydaniel.taskflow.project.exception.ProjectAccessDeniedException;
import com.thelazydaniel.taskflow.project.exception.ProjectArchivedException;
import com.thelazydaniel.taskflow.project.exception.ProjectDeletedException;
import com.thelazydaniel.taskflow.project.exception.ProjectIdNotFoundException;
import com.thelazydaniel.taskflow.task.TaskService;
import com.thelazydaniel.taskflow.user.entity.User;
import com.thelazydaniel.taskflow.user.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final TaskService taskService;

    public ProjectService(ProjectRepository projectRepository, ProjectMapper projectMapper, TaskService taskService) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.taskService = taskService;
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request){
        request.validateDates();
        User currentUser = SecurityUtils.getCurrentUser();
        Project project = projectMapper.toEntity(request);
        project.setStatus(ProjectStatus.ACTIVE);
        //default
        project.setOwner(currentUser);
        projectRepository.save(project);
        return projectMapper.toProjectResponse(project);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectSummaryResponse> getAllProjects(PageRequest request){
        Pageable pageable = request.toPageable();
        Page<Project> projects = projectRepository.findAll(pageable);
        return PageResponse.from(projects, projectMapper::toProjectSummaryResponse);
    }

    @Transactional(readOnly = true)
    public ProjectResponse findProjectById(long id){
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectIdNotFoundException(id));
        return projectMapper.toProjectResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(
            UpdateProjectRequest request,
            long id){
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectIdNotFoundException(id));
        if (project.getStatus().equals(ProjectStatus.ARCHIVED)){
            throw new ProjectArchivedException("You cannot update an archived project");
        }
        if (project.getStatus().equals(ProjectStatus.DELETED)){
            throw new ProjectDeletedException("You cannot update an deleted project");
        }
        checkAccess(project,"update");
        projectMapper.updateEntity(request,project);
        projectRepository.save(project);
        return projectMapper.toProjectResponse(project);
    }

    @Transactional
    public ProjectResponse deleteProject(long id){
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectIdNotFoundException(id));
        project.setStatus(ProjectStatus.DELETED);
        checkAccess(project,"delete");
        projectRepository.save(project);
        return projectMapper.toProjectResponse(project);
    }

    @Transactional
    public ProjectResponse archiveProject(long id){
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectIdNotFoundException(id));
        project.setStatus(ProjectStatus.ARCHIVED);
        projectRepository.save(project);
        return projectMapper.toProjectResponse(project);
    }

    @Transactional
    public ProjectResponse restoreProject(long id){
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectIdNotFoundException(id));
        project.setStatus(ProjectStatus.ACTIVE);
        projectRepository.save(project);
        return projectMapper.toProjectResponse(project);
    }

    @Transactional(readOnly = true)
    public void findAllRelatedTasks(long id){
        taskService.findTasksByProject(id);
        //implement later
    }

    private void checkAccess(Project project, String operation) {
        User currentUser = SecurityUtils.getCurrentUser();

        if (project.getOwner().getId().equals(currentUser.getId()) ||
                currentUser.getRole() == UserRole.ADMIN) {
            return;
        }

        throw new ProjectAccessDeniedException(
                String.format("Only the project owner or admin can %s this project", operation)
        );
    }
}
