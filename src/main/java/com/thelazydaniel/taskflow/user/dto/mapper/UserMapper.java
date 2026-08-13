package com.thelazydaniel.taskflow.user.dto.mapper;


import com.thelazydaniel.taskflow.user.entity.User;
import com.thelazydaniel.taskflow.auth.dto.request.RegisterRequest;
import com.thelazydaniel.taskflow.user.dto.request.UpdateUserRequest;
import com.thelazydaniel.taskflow.user.dto.response.UserAdminResponse;
import com.thelazydaniel.taskflow.user.dto.response.UserManagerResponse;
import com.thelazydaniel.taskflow.user.dto.response.UserResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {


    User toEntity(RegisterRequest request);


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
