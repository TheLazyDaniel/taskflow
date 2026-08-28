package com.thelazydaniel.taskflow.user.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.thelazydaniel.taskflow.user.enums.UserRole;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserManagerResponse(
      Long id,
      String username,
      String email,
      String firstName,
      String lastName,
      UserRole role,

      @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
      LocalDateTime createdDate,

      @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
      LocalDateTime lastLoginDate,

      @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
      LocalDateTime updatedDate

) implements UserResponse {
}