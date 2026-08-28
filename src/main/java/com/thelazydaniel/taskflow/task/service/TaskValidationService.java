package com.thelazydaniel.taskflow.task.service;

import com.thelazydaniel.taskflow.common.util.SecurityUtils;
import com.thelazydaniel.taskflow.task.TaskRepository;
import com.thelazydaniel.taskflow.task.entity.Task;
import com.thelazydaniel.taskflow.task.enums.TaskStatus;
import com.thelazydaniel.taskflow.task.exception.AssignTaskDeniedException;
import com.thelazydaniel.taskflow.task.exception.InvalidStatusTransitionException;
import com.thelazydaniel.taskflow.task.exception.TaskFieldUpdateDeniedException;
import com.thelazydaniel.taskflow.task.exception.TaskStatusTransitionDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskValidationService {

    private final TaskRepository taskRepository;

    public long getCurrentUserId(){
        return SecurityUtils.getCurrentUserId();
    }

    public void canAssignTask(){
        if (!SecurityUtils.hasRole("MANAGER")){
            throw new AssignTaskDeniedException("Only MANAGER is allowed to assign tasks");
        }
    }
    public boolean isActiveTasksUnderProject(Long projectId){
        return taskRepository.existsByProjectIdAndStatusOrStatus(projectId, TaskStatus.TODO,TaskStatus.IN_PROGRESS);
    }

    public boolean isProgressingTasksUnderProject(Long projectId){
        return taskRepository.existsByProjectIdAndStatus(projectId, TaskStatus.IN_PROGRESS);
    }

    public boolean isBelongProjectRelatedTask(Long projectId){
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return taskRepository.existsByAssigneeIdOrReporterIdAndProjectId(currentUserId,projectId);
    }

    public void validateTaskStatusTransition(Task task, TaskStatus targetStatus) {
        TaskStatus currentStatus = task.getStatus();

        if (currentStatus == targetStatus) {
            return;
        }

        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new InvalidStatusTransitionException(
                    String.format("Cannot transition the status of task from %s to %s", currentStatus, targetStatus)
            );
        }

        // Additional checks
        switch (targetStatus) {
            case IN_PROGRESS -> {
                if (task.getAssigneeId() == null) {
                    throw new TaskStatusTransitionDeniedException("Assignee is required to change the status of task to TODO");
                }
                if (task.getDueDate() == null) {
                    throw new TaskStatusTransitionDeniedException("Due date is required to change the status of task to TODO");
                }
            }
            case DONE -> {
                if (!task.getAssigneeId().equals(SecurityUtils.getCurrentUserId())) {
                    throw new TaskStatusTransitionDeniedException("Only assignee can mark the status of task as DONE");
                }
            }
            case TODO -> {
                if (!SecurityUtils.hasRole("MANAGER") && !SecurityUtils.hasRole("ADMIN")) {
                    throw new TaskStatusTransitionDeniedException("Only MANAGER/ADMIN can reopen a task");
                }
            }
        }
    }

    public void validateTaskUpdateFields(Task task, boolean isOwner, String requestTitle){
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (SecurityUtils.hasRole("MANAGER") || isOwner){
            return;
        }
        Long taskAssigneeId = task.getAssigneeId();
        boolean isAssignee = Objects.nonNull(taskAssigneeId) && Objects.equals(currentUserId,taskAssigneeId);
        boolean isTitleNotModified = Objects.equals(requestTitle, task.getTitle());

        if (isAssignee && !isTitleNotModified){
            throw new TaskFieldUpdateDeniedException("if you are not a MANAGER or a project owner of this task, " +
                    "assignee cannot update title of this task");
        }
    }
}
