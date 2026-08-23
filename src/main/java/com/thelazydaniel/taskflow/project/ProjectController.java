package com.thelazydaniel.taskflow.project;

import com.thelazydaniel.taskflow.common.dto.request.PageRequest;
import com.thelazydaniel.taskflow.common.dto.response.PageResponse;
import com.thelazydaniel.taskflow.project.dto.request.CreateProjectRequest;
import com.thelazydaniel.taskflow.project.dto.request.UpdateProjectRequest;
import com.thelazydaniel.taskflow.project.dto.response.ProjectPublicResponse;
import com.thelazydaniel.taskflow.project.dto.response.ProjectResponse;
import com.thelazydaniel.taskflow.project.dto.response.ProjectSummaryResponse;
import com.thelazydaniel.taskflow.project.service.ProjectService;
import com.thelazydaniel.taskflow.task.dto.response.TaskSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest createProjectRequest){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(projectService.createProject(createProjectRequest));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProjectSummaryResponse>> getAllProjects(
            @Valid PageRequest pageRequest) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(projectService.getAllProjects(pageRequest));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable long id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(projectService.findProjectById(id));
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<ProjectResponse> updateProjectById(
            @Valid @RequestBody UpdateProjectRequest updateProjectRequest,
            @PathVariable long id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(projectService.updateProject(updateProjectRequest,id));
    }


    @DeleteMapping(value = "/{id}")
    public ResponseEntity<ProjectResponse> deleteProjectById(@PathVariable long id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(projectService.deleteProject(id));
    }

    @PostMapping(value = "/{id}/archive")
    public ResponseEntity<ProjectResponse> archiveProjectById(@PathVariable long id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(projectService.archiveProject(id));
    }

    @PostMapping(value = "/{id}/restore")
    public ResponseEntity<ProjectResponse> restoreProjectById(@PathVariable long id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(projectService.restoreProject(id));
    }

    @GetMapping(value = "/{id}/tasks")
    public ResponseEntity<PageResponse<TaskSummaryResponse>> getTasksByProjectId(
            @Valid PageRequest pageRequest,
            @PathVariable long id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(projectService.findAllRelatedTasks(pageRequest,id));
    }
}
