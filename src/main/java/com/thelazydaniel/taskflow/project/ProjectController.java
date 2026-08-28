package com.thelazydaniel.taskflow.project;

import com.thelazydaniel.taskflow.common.dto.request.PageRequest;
import com.thelazydaniel.taskflow.common.dto.response.PageResponse;
import com.thelazydaniel.taskflow.project.dto.request.CreateProjectRequest;
import com.thelazydaniel.taskflow.project.dto.request.UpdateProjectRequest;
import com.thelazydaniel.taskflow.project.dto.response.ProjectResponse;
import com.thelazydaniel.taskflow.project.dto.response.ProjectSummaryResponse;
import com.thelazydaniel.taskflow.project.service.ProjectService;
import com.thelazydaniel.taskflow.task.dto.response.TaskSummaryResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/projects")
@Slf4j
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @PreAuthorize("hasPermission('PROJECT','CREATE')")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest createProjectRequest){
        log.info("Creating project");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(projectService.createProject(createProjectRequest));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<ProjectSummaryResponse>> getAllProjects(
            @Valid @RequestBody PageRequest pageRequest) {
        log.debug("Listing projects: page={}, size={}", pageRequest.page(), pageRequest.size());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(projectService.getAllProjects(pageRequest));
    }

    @GetMapping(value = "/{id}")
    @PreAuthorize("hasPermission(#id,'PROJECT','READ')")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable long id){
        log.debug("Fetching project: id={}", id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(projectService.findProjectById(id));
    }

    @PutMapping(value = "/{id}")
    @PreAuthorize("hasPermission(#id,'PROJECT','READ')")
    public ResponseEntity<ProjectResponse> updateProjectById(
            @Valid @RequestBody UpdateProjectRequest updateProjectRequest,
            @PathVariable long id){
        log.info("Updating project: id={}", id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(projectService.updateProject(updateProjectRequest,id));
    }


    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasPermission(#id,'PROJECT','DELETE')")
    public ResponseEntity<ProjectResponse> deleteProjectById(@PathVariable long id){
        log.info("Deleting project: id={}", id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(projectService.deleteProject(id));
    }

    @PostMapping(value = "/{id}/archive")
    @PreAuthorize("hasPermission(#id,'PROJECT','UPDATE')")
    public ResponseEntity<ProjectResponse> archiveProjectById(@PathVariable long id){
        log.info("Archiving project: id={}", id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(projectService.archiveProject(id));
    }

    @PostMapping(value = "/{id}/restore")
    @PreAuthorize("hasPermission(#id,'PROJECT','UPDATE')")
    public ResponseEntity<ProjectResponse> restoreProjectById(@PathVariable long id){
        log.info("Restoring project: id={}", id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(projectService.restoreProject(id));
    }

    @GetMapping(value = "/{id}/tasks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<TaskSummaryResponse>> getTasksByProjectId(
            @Valid PageRequest pageRequest,
            @PathVariable long id){
        log.debug("Listing tasks for project: id={}, page={}, size={}",
                id, pageRequest.page(), pageRequest.size());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(projectService.findAllRelatedTasks(pageRequest,id));
    }
}
