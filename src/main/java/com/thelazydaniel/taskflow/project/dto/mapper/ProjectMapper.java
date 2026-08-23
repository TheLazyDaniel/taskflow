package com.thelazydaniel.taskflow.project.dto.mapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thelazydaniel.taskflow.common.util.SecurityUtils;
import com.thelazydaniel.taskflow.project.dto.request.CreateProjectRequest;
import com.thelazydaniel.taskflow.project.dto.request.UpdateProjectRequest;
import com.thelazydaniel.taskflow.project.dto.response.ProjectSummaryResponse;

import com.thelazydaniel.taskflow.project.entity.Project;
import com.thelazydaniel.taskflow.user.enums.UserRole;
import org.mapstruct.*;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface ProjectMapper {

    Project toEntity(CreateProjectRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateProjectRequest request, @MappingTarget Project project);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    @Mapping(target = "ownerId", source = "ownerId")
    ProjectSummaryResponse toProjectSummaryResponse(Project project);


    default LocalDateTime mapUpdatedDate(Project project) {
        UserRole currentRole = SecurityUtils.getCurrentUserRole();
        if (currentRole == UserRole.ADMIN || currentRole == UserRole.MANAGER) {
            return project.getUpdatedDate();
        }
        return null;
    }
}
