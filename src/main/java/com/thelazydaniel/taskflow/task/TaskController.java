package com.thelazydaniel.taskflow.task;

import com.thelazydaniel.taskflow.common.dto.request.PageRequest;
import com.thelazydaniel.taskflow.common.dto.response.PageResponse;
import com.thelazydaniel.taskflow.task.dto.request.AssignTaskRequest;
import com.thelazydaniel.taskflow.task.dto.request.CreateTaskRequest;
import com.thelazydaniel.taskflow.task.dto.request.UpdateTaskRequest;
import com.thelazydaniel.taskflow.task.dto.request.UpdateTaskStatusRequest;
import com.thelazydaniel.taskflow.task.dto.response.TaskResponse;
import com.thelazydaniel.taskflow.task.dto.response.TaskSummaryResponse;
import com.thelazydaniel.taskflow.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping(value = "/tasks")
@Slf4j
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @PreAuthorize("hasPermission('TASK','CREATE')")
    public ResponseEntity<TaskResponse> createNewTask(
            @Valid @RequestBody CreateTaskRequest request){
        log.info("Creating task: projectId={}", request.projectId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskService.createTask(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<TaskSummaryResponse>> getAllTasks(
            @Valid @RequestBody PageRequest request){
        log.debug("Listing tasks: page={}, size={}", request.page(), request.size());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.getAllTasks(request));

    }

    @GetMapping(value = "/{id}")
    @PreAuthorize("hasPermission(#id,'TASK','READ')")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable long id){
        log.debug("Fetching task: id={}", id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.getTaskById(id));

    }

    @PutMapping(value = "/{id}")
    @PreAuthorize("hasPermission(#id,'TASK','UPDATE')")
    public ResponseEntity<TaskResponse> updateTaskById(
            @PathVariable long id,
            @Valid @RequestBody UpdateTaskRequest updateTaskRequest){
        log.debug("Update task: id={}", id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.updateTaskById(updateTaskRequest,id));

    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasPermission(#id,'TASK','DELETE')")
    public ResponseEntity<String> deleteTaskById(
            @PathVariable long id){
        log.info("Deleting task: id={}", id);
        return ResponseEntity.ok(taskService.deleteTaskById(id));

    }

    @PutMapping(value = "/{id}/status")
    @PreAuthorize("hasPermission(#id,'TASK','UPDATE_STATUS')")
    public ResponseEntity<TaskResponse> updateTaskStatusById(
            @PathVariable long id,
            @Valid @RequestBody UpdateTaskStatusRequest request){
        log.info("Updating task status: id={}, status={}", id, request.status());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.updateTaskStatusById(request,id));

    }

    @PutMapping(value = "/{id}/assign")
    @PreAuthorize("hasPermission(#id,'TASK','ASSIGN')")
    public ResponseEntity<TaskResponse> assignTaskStatusById(
            @PathVariable long id,
            @Valid @RequestBody AssignTaskRequest request){
        log.info("Assigning task: id={}, assigneeId={}", id, request.assigneeId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.assignTaskById(request,id));

    }

    @GetMapping(value = "/assigned/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<TaskSummaryResponse>> getTasksAssignedToSelf(
            @Valid @RequestBody PageRequest request){
        log.debug("Listing tasks assigned to current user: page={}, size={}",
                request.page(), request.size());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.getAssignedTasksSelf(request));
    }

    @GetMapping(value = "/reported/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<TaskSummaryResponse>> getTasksReportedBySelf(
            @Valid @RequestBody PageRequest request){
        log.debug("Listing tasks reported by current user: page={}, size={}",
                request.page(), request.size());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.getReportedTasksSelf(request));
    }
}
