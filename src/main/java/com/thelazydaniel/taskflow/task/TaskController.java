package com.thelazydaniel.taskflow.task;

import com.thelazydaniel.taskflow.common.dto.request.PageRequest;
import com.thelazydaniel.taskflow.common.dto.response.PageResponse;
import com.thelazydaniel.taskflow.task.dto.request.AssignTaskRequest;
import com.thelazydaniel.taskflow.task.dto.request.CreateTaskRequest;
import com.thelazydaniel.taskflow.task.dto.request.UpdateTaskStatusRequest;
import com.thelazydaniel.taskflow.task.dto.response.TaskResponse;
import com.thelazydaniel.taskflow.task.dto.response.TaskSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping(value = "/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createNewTask(
            @Valid @RequestBody CreateTaskRequest request){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskService.createTask(request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<TaskSummaryResponse>> getAllTasks(
            @Valid @RequestBody PageRequest request){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.getAllTasks(request));

    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable long id){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.getTaskById(id));

    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> deleteTaskById(
            @PathVariable long id){
        return ResponseEntity.ok(taskService.deleteTaskById(id));

    }

    @PutMapping(value = "/{id}/status")
    public ResponseEntity<TaskResponse> updateTaskStatusById(
            @PathVariable long id,
            @Valid @RequestBody UpdateTaskStatusRequest request){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.updateTaskStatusById(request,id));

    }

    @PutMapping(value = "/{id}/assign")
    public ResponseEntity<TaskResponse> assignTaskStatusById(
            @PathVariable long id,
            @Valid @RequestBody AssignTaskRequest request){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.assignTaskById(request,id));

    }

    @GetMapping(value = "/assigned/me")
    public ResponseEntity<PageResponse<TaskSummaryResponse>> getTasksAssignedToSelf(
            @Valid @RequestBody PageRequest request){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.getAssignedTasksSelf(request));
    }

    @GetMapping(value = "/reported/me")
    public ResponseEntity<PageResponse<TaskSummaryResponse>> getTasksReportedBySelf(
            @Valid @RequestBody PageRequest request){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.getReportedTasksSelf(request));
    }
}
