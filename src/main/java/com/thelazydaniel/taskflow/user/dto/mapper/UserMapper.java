package com.thelazydaniel.taskflow.user.dto.mapper;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.thelazydaniel.taskflow.common.util.SecurityUtils;
import com.thelazydaniel.taskflow.user.dto.response.UserSummaryResponse;
import com.thelazydaniel.taskflow.user.entity.User;
import com.thelazydaniel.taskflow.auth.dto.request.RegisterRequest;
import com.thelazydaniel.taskflow.user.dto.request.UpdateUserRequest;
import com.thelazydaniel.taskflow.user.dto.response.UserResponse;
import com.thelazydaniel.taskflow.user.enums.UserRole;
import org.mapstruct.*;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface UserMapper {


    User toEntity(RegisterRequest request);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateUserRequest request, @MappingTarget User user);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "createdDate", source = "createdDate")
    @Mapping(target = "updatedDate",
            expression = "java(mapUpdatedDate(user))")
    @Mapping(target = "lastLoginDate",
            expression = "java(mapLastLoginDate(user))")
    @Mapping(target = "enabled",
            expression = "java(mapEnabled(user))")
    @Mapping(target = "accountNonLocked",
            expression = "java(mapAccountNonLocked(user))")
    UserSummaryResponse toUserSummaryResponse(User user);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "createdDate", source = "createdDate")
    @Mapping(target = "firstName",
            expression = "java(user.getFirstName() != null ? user.getFirstName() : \"First name is not added\")")
    @Mapping(target = "lastName",
            expression = "java(user.getLastName() != null ? user.getLastName() : \"Last name is not added\")")
    @Mapping(target = "email",
            expression = "java(user.getEmail() != null ? user.getEmail() : \"Email is not added\")")
    @Mapping(target = "updatedDate",
            expression = "java(mapUpdatedDate(user))")
    @Mapping(target = "lastLoginDate",
            expression = "java(mapLastLoginDate(user))")
    @Mapping(target = "enabled",
            expression = "java(mapEnabled(user))")
    @Mapping(target = "accountNonLocked",
            expression = "java(mapAccountNonLocked(user))")
    UserResponse toUserResponse(User user);


    // --- For Detail (Single user view) ---

    default Boolean mapEnabled(User user) {
        UserRole currentRole = SecurityUtils.getCurrentUserRole();

        // Admins
        if (currentRole == UserRole.ADMIN) {
            return user.isEnabled();
        }
        return null;
    }

    default Boolean mapAccountNonLocked(User user) {
        UserRole currentRole = SecurityUtils.getCurrentUserRole();

        if (currentRole == UserRole.ADMIN) {
            return user.isAccountNonLocked();
        }
        return null;
    }

    default LocalDateTime mapLastLoginDate(User user) {
        UserRole currentRole = SecurityUtils.getCurrentUserRole();
        // Only admins and managers can see this
        if (currentRole == UserRole.ADMIN || currentRole == UserRole.MANAGER) {
            return user.getLastLoginDate();
        }
        return null;
    }

    default LocalDateTime mapUpdatedDate(User user) {
        UserRole currentRole = SecurityUtils.getCurrentUserRole();
        if (currentRole == UserRole.ADMIN || currentRole == UserRole.MANAGER) {
            return user.getUpdatedDate();
        }
        return null;
    }



}
