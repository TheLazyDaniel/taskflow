package com.thelazydaniel.taskflow.dto.mapper;

import com.thelazydaniel.taskflow.domain.entity.User;
import com.thelazydaniel.taskflow.dto.request.CreateUserRequest;
import com.thelazydaniel.taskflow.dto.request.UpdateUserRequest;
import com.thelazydaniel.taskflow.dto.response.UserAdminResponse;
import com.thelazydaniel.taskflow.dto.response.UserManagerResponse;
import com.thelazydaniel.taskflow.dto.response.UserResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {


    User toEntity(CreateUserRequest request);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateUserRequest request, @MappingTarget User user);

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
    UserResponse toUserResponse(User user);


    @InheritConfiguration(name = "toUserResponse")
    @Mapping(target = "updatedDate", source = "updatedDate")
    @Mapping(target = "lastLoginDate", source = "lastLoginDate")
    UserManagerResponse toUserManagerResponse(User user);

    @InheritConfiguration(name = "toUserResponse")
    @Mapping(target = "updatedDate", source = "updatedDate")
    @Mapping(target = "lastLoginDate", source = "lastLoginDate")
    @Mapping(target = "enabled", source = "enabled")
    @Mapping(target = "accountNonLocked", source = "accountNonLocked")
    UserAdminResponse toUserAdminResponse(User user);
}
