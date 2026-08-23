package com.thelazydaniel.taskflow.task.dto.mapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thelazydaniel.taskflow.task.dto.request.CreateTaskRequest;
import com.thelazydaniel.taskflow.task.dto.request.UpdateTaskRequest;
import com.thelazydaniel.taskflow.task.dto.response.TaskResponse;
import com.thelazydaniel.taskflow.task.dto.response.TaskSummaryResponse;
import com.thelazydaniel.taskflow.task.entity.Task;
import org.mapstruct.*;


@Mapper(componentModel = "spring")
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface TaskMapper {

    Task toEntity(CreateTaskRequest createTaskRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateTaskRequest updateTaskRequest, @MappingTarget Task task);

    TaskResponse toTaskResponse(Task task);

    TaskSummaryResponse toTaskSummaryResponse(Task task);

}
