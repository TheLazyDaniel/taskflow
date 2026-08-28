package com.thelazydaniel.taskflow.task.service;

import com.thelazydaniel.taskflow.common.dto.request.PageRequest;
import com.thelazydaniel.taskflow.common.dto.response.PageResponse;
import com.thelazydaniel.taskflow.project.service.ProjectValidationService;
import com.thelazydaniel.taskflow.task.TaskRepository;
import com.thelazydaniel.taskflow.task.dto.mapper.TaskMapper;
import com.thelazydaniel.taskflow.task.dto.request.AssignTaskRequest;
import com.thelazydaniel.taskflow.task.dto.request.CreateTaskRequest;
import com.thelazydaniel.taskflow.task.dto.request.UpdateTaskRequest;
import com.thelazydaniel.taskflow.task.dto.request.UpdateTaskStatusRequest;
import com.thelazydaniel.taskflow.task.dto.response.TaskResponse;
import com.thelazydaniel.taskflow.task.dto.response.TaskSummaryResponse;
import com.thelazydaniel.taskflow.task.entity.Task;
import com.thelazydaniel.taskflow.task.enums.TaskStatus;
import com.thelazydaniel.taskflow.task.exception.CreateTaskDeniedException;
import com.thelazydaniel.taskflow.task.exception.TaskAssigneeConflictException;
import com.thelazydaniel.taskflow.task.exception.TaskIdNotFoundException;
import com.thelazydaniel.taskflow.task.exception.TaskInvalidOperationException;
import com.thelazydaniel.taskflow.user.service.UserValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final ProjectValidationService projectValidationService;
    private final UserValidationService userValidationService;
    private final TaskValidationService taskValidationService;

    public TaskService(TaskRepository taskRepository,
                       TaskMapper taskMapper,
                       ProjectValidationService projectValidationService,
                       UserValidationService userValidationService,
                       TaskValidationService taskValidationService) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.projectValidationService = projectValidationService;
        this.userValidationService = userValidationService;
        this.taskValidationService = taskValidationService;
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskSummaryResponse> findTasksByProject(PageRequest pageRequest, long id){
        log.debug("Finding tasks for project: projectId={}, page={}, size={}",
                id, pageRequest.page(), pageRequest.size());
        Pageable pageable = pageRequest.toPageable();
        Page<Task> tasks = taskRepository.findAllByProject_Id(id,pageable);
        return PageResponse.from(tasks, taskMapper::toTaskSummaryResponse);

    }

    @Transactional
    public void DetachTasksByProjectId(Long id){
        taskRepository.detachFromProject(id);
    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request){
        log.info("Creating task: projectId={}, assigneeId={}", request.projectId(), request.assigneeId());

        Long currentProjectId = projectValidationService.validateAndGetProjectId(request.projectId());
        boolean isProjectMember = projectValidationService.validateAddTaskToAccessibleProject(request.projectId()) ||
                taskValidationService.isBelongProjectRelatedTask(request.projectId());

        if (!isProjectMember){
            throw new CreateTaskDeniedException("You are not accessible to this project. You cannot create task on it");
        }
        projectValidationService.validateAddTaskAvailability(request.projectId());

        Task task = taskMapper.toEntity(request);
        if (request.assigneeId() != null){
            taskValidationService.canAssignTask();
            userValidationService.validateUser(request.assigneeId());
            task.setAssigneeId(request.assigneeId());
        }
        task.setStatus(TaskStatus.TODO);
        //default
        Long currentUserId = taskValidationService.getCurrentUserId();
        if (currentUserId.equals(request.assigneeId())){
            throw new TaskAssigneeConflictException("Reporter cannot be the same as assignee");
        }
        task.setReporterId(userValidationService.getCurrentUserId());
        task.setProjectId(currentProjectId);
        Task currentTask = taskRepository.save(task);
        log.info("Task created: taskId={}", currentTask.getId());
        return taskMapper.toTaskResponse(currentTask);
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskSummaryResponse> getAllTasks(PageRequest request) {
        log.debug("Finding all tasks: page={}, size={}", request.page(), request.size());
        Pageable pageable = request.toPageable();
        Page<Task> tasks = taskRepository.findAll(pageable);
        return PageResponse.from(tasks,taskMapper::toTaskSummaryResponse);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(long id){
        log.debug("Finding task: id={}", id);
        Task task = taskRepository.findById(id).orElseThrow(
                ()-> new TaskIdNotFoundException(id));
        return taskMapper.toTaskResponse(task);
    }

    @Transactional
    public TaskResponse updateTaskById(UpdateTaskRequest request,long id){
        log.info("Updating task: id={}", id);
        Task task = taskRepository.findById(id).orElseThrow(
                ()-> new TaskIdNotFoundException(id));
        if (task.getStatus().equals(TaskStatus.IN_PROGRESS)){
            throw new TaskInvalidOperationException("You cannot update an IN_PROGRESS task");
        }
        taskValidationService.validateTaskUpdateFields(task,
                projectValidationService.isOwnerOfProject(task.getProjectId()),
                request.title());
        taskMapper.updateEntity(request,task);
        Task currentTask = taskRepository.save(task);
        return taskMapper.toTaskResponse(currentTask);
    }

    @Transactional
    public String deleteTaskById(long id){
        log.info("Deleting task: id={}", id);
        Task task = taskRepository.findById(id).orElseThrow(
                ()-> new TaskIdNotFoundException(id));
        if (task.getStatus().equals(TaskStatus.IN_PROGRESS)){
            throw new TaskInvalidOperationException("You cannot delete an IN_PROGRESS task");
        }
        taskRepository.delete(task);
        return "Deleted";
    }

    @Transactional
    public TaskResponse updateTaskStatusById(UpdateTaskStatusRequest request, long id){
        log.info("Updating task status: id={}, status={}", id, request.status());
        Task task = taskRepository.findById(id).orElseThrow(
                ()-> new TaskIdNotFoundException(id));
        taskValidationService.validateTaskStatusTransition(task,request.status());
        task.setStatus(request.status());
        Task currentTask = taskRepository.save(task);
        return taskMapper.toTaskResponse(currentTask);
    }

    @Transactional
    public TaskResponse assignTaskById(AssignTaskRequest request, long id){
        log.info("Assigning task: id={}, assigneeId={}", id, request.assigneeId());
        Task task = taskRepository.findById(id).orElseThrow(
                ()-> new TaskIdNotFoundException(id));
        userValidationService.validateUser(request.assigneeId());
        if (task.getReporterId().equals(request.assigneeId())){
            throw new TaskAssigneeConflictException("Reporter cannot be the same as assignee");
        }
        task.setAssigneeId(request.assigneeId());
        Task currentTask = taskRepository.save(task);
        return taskMapper.toTaskResponse(currentTask);
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskSummaryResponse> getAssignedTasksSelf(PageRequest request){
        Long currentUserId = taskValidationService.getCurrentUserId();
        log.debug("Finding tasks assigned to user: userId={}, page={}, size={}",
                currentUserId, request.page(), request.size());
        Pageable pageable = request.toPageable();
        Page<Task> tasks = taskRepository.findAllByAssignee_Id(currentUserId,pageable);
        return PageResponse.from(tasks,taskMapper::toTaskSummaryResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskSummaryResponse> getReportedTasksSelf(PageRequest request){
        Long currentUserId = taskValidationService.getCurrentUserId();
        log.debug("Finding tasks reported by user: userId={}, page={}, size={}",
                currentUserId, request.page(), request.size());
        Pageable pageable = request.toPageable();
        Page<Task> tasks = taskRepository.findAllByReporter_Id(currentUserId,pageable);
        return PageResponse.from(tasks,taskMapper::toTaskSummaryResponse);
    }
}
