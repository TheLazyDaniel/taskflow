package com.thelazydaniel.taskflow.task;

import com.thelazydaniel.taskflow.common.dto.request.PageRequest;
import com.thelazydaniel.taskflow.common.dto.response.PageResponse;
import com.thelazydaniel.taskflow.common.util.SecurityUtils;
import com.thelazydaniel.taskflow.project.service.ProjectValidationService;
import com.thelazydaniel.taskflow.task.dto.mapper.TaskMapper;
import com.thelazydaniel.taskflow.task.dto.request.AssignTaskRequest;
import com.thelazydaniel.taskflow.task.dto.request.CreateTaskRequest;
import com.thelazydaniel.taskflow.task.dto.request.UpdateTaskRequest;
import com.thelazydaniel.taskflow.task.dto.request.UpdateTaskStatusRequest;
import com.thelazydaniel.taskflow.task.dto.response.TaskResponse;
import com.thelazydaniel.taskflow.task.dto.response.TaskSummaryResponse;
import com.thelazydaniel.taskflow.task.entity.Task;
import com.thelazydaniel.taskflow.task.enums.TaskStatus;
import com.thelazydaniel.taskflow.task.exception.TaskAssigneeConflictException;
import com.thelazydaniel.taskflow.task.exception.TaskIdNotFoundException;
import com.thelazydaniel.taskflow.user.service.UserValidationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final ProjectValidationService projectValidationService;
    private final UserValidationService userValidationService;

    public TaskService(TaskRepository taskRepository,
                       TaskMapper taskMapper,
                       ProjectValidationService projectValidationService, UserValidationService userValidationService) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.projectValidationService = projectValidationService;
        this.userValidationService = userValidationService;
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskSummaryResponse> findTasksByProject(PageRequest pageRequest, long id){
        Pageable pageable = pageRequest.toPageable();
        Page<Task> tasks = taskRepository.findAllByProject_Id(id,pageable);
        return PageResponse.from(tasks, taskMapper::toTaskSummaryResponse);

    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request){

        projectValidationService.validateAddTaskAvailability(request.projectId());

        Task task = taskMapper.toEntity(request);
        if (request.assigneeId() != null){
            task.setAssignee(userValidationService.validateAndGetUser(request.assigneeId()));
        }
        task.setStatus(TaskStatus.TODO);
        //default
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId.equals(request.assigneeId())){
            throw new TaskAssigneeConflictException("Reporter cannot be the same as assignee");
        }
        task.setReporter(userValidationService.validateAndGetUser(currentUserId));
        task.setProject(projectValidationService.validateAndGetProject(request.projectId()));
        taskRepository.save(task);
        return taskMapper.toTaskResponse(task);
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskSummaryResponse> getAllTasks(PageRequest request) {
        Pageable pageable = request.toPageable();
        Page<Task> tasks = taskRepository.findAll(pageable);
        return PageResponse.from(tasks,taskMapper::toTaskSummaryResponse);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(long id){
        Task task = taskRepository.findById(id).orElseThrow(
                ()-> new TaskIdNotFoundException(id));
        return taskMapper.toTaskResponse(task);
    }

    @Transactional
    public TaskResponse updateTaskById(UpdateTaskRequest request,long id){
        Task task = taskRepository.findById(id).orElseThrow(
                ()-> new TaskIdNotFoundException(id));
        taskMapper.updateEntity(request,task);
        Task currentTask = taskRepository.save(task);
        return taskMapper.toTaskResponse(currentTask);
    }

    @Transactional
    public String deleteTaskById(long id){
        Task task = taskRepository.findById(id).orElseThrow(
                ()-> new TaskIdNotFoundException(id));
        taskRepository.delete(task);
        return "Potential Warning, implement after Permission Evaluator";
    }

    @Transactional
    public TaskResponse updateTaskStatusById(UpdateTaskStatusRequest request, long id){
        Task task = taskRepository.findById(id).orElseThrow(
                ()-> new TaskIdNotFoundException(id));
        task.setStatus(request.status());
        Task currentTask = taskRepository.save(task);
        return taskMapper.toTaskResponse(currentTask);
    }

    @Transactional
    public TaskResponse assignTaskById(AssignTaskRequest request, long id){
        Task task = taskRepository.findById(id).orElseThrow(
                ()-> new TaskIdNotFoundException(id));
        task.setAssigneeId(request.assigneeId());
        Task currentTask = taskRepository.save(task);
        return taskMapper.toTaskResponse(currentTask);
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskSummaryResponse> getAssignedTasksSelf(PageRequest request){
        Long currentUserId =  SecurityUtils.getCurrentUserId();
        Pageable pageable = request.toPageable();
        Page<Task> tasks = taskRepository.findAllByAssignee_Id(currentUserId,pageable);
        return PageResponse.from(tasks,taskMapper::toTaskSummaryResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<TaskSummaryResponse> getReportedTasksSelf(PageRequest request){
        Long currentUserId =  SecurityUtils.getCurrentUserId();
        Pageable pageable = request.toPageable();
        Page<Task> tasks = taskRepository.findAllByReporter_Id(currentUserId,pageable);
        return PageResponse.from(tasks,taskMapper::toTaskSummaryResponse);
    }
}
