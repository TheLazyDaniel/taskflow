package com.thelazydaniel.taskflow.user.dto.mapper;


import com.thelazydaniel.taskflow.common.util.SecurityUtils;
import com.thelazydaniel.taskflow.user.dto.response.*;
import com.thelazydaniel.taskflow.user.entity.User;
import com.thelazydaniel.taskflow.auth.dto.request.RegisterRequest;
import com.thelazydaniel.taskflow.user.dto.request.UpdateUserRequest;
import com.thelazydaniel.taskflow.user.enums.UserRole;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountNonLocked", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "lastLoginDate", ignore = true)
    User toEntity(RegisterRequest request);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @InheritConfiguration(name = "toEntity")
    void updateEntity(UpdateUserRequest request, @MappingTarget User user);


    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "createdDate", source = "createdDate")
    @Mapping(target = "lastLoginDate", source = "lastLoginDate")
    UserPublicResponse toUserPublicResponse(User user);

    @InheritConfiguration(name = "toUserPublicResponse")
    @Mapping(target = "updatedDate", source = "updatedDate")
    UserManagerResponse toUserManagerResponse(User user);

    @InheritConfiguration(name = "toUserPublicResponse")
    @Mapping(target = "updatedDate", source = "updatedDate")
    @Mapping(target = "enabled", source = "enabled")
    @Mapping(target = "accountNonLocked", source = "accountNonLocked")
    UserAdminResponse toUserAdminResponse(User user);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "role", source = "role")
    UserPublicSummaryResponse toUserPublicSummaryResponse(User user);

    @InheritConfiguration(name = "toUserPublicSummaryResponse")
    @Mapping(target = "enabled", source = "enabled")
    @Mapping(target = "accountNonLocked", source = "accountNonLocked")
    UserAdminSummaryResponse toUserAdminSummaryResponse(User user);

    default UserResponse toResponse(User user) {
        if (user == null) return null;

        UserRole currentRole = SecurityUtils.getCurrentUserRole();

        return switch (currentRole) {
            case ADMIN -> toUserAdminResponse(user);
            case MANAGER -> toUserManagerResponse(user);
            default -> toUserPublicResponse(user);
        };
    }

    default UserSummaryResponse toSummaryResponse(User user) {
        if (user == null) return null;

        UserRole currentRole = SecurityUtils.getCurrentUserRole();

        if (currentRole.equals(UserRole.ADMIN)) {
            return toUserAdminSummaryResponse(user);
        } else {
            return toUserPublicSummaryResponse(user);
        }
    }
}
