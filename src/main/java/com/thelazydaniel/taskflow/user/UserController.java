package com.thelazydaniel.taskflow.user;


import com.thelazydaniel.taskflow.common.util.SecurityUtils;
import com.thelazydaniel.taskflow.common.dto.request.PageRequest;
import com.thelazydaniel.taskflow.user.dto.request.UpdateUserRequest;
import com.thelazydaniel.taskflow.user.dto.request.UpdateUserRoleRequest;
import com.thelazydaniel.taskflow.common.dto.response.PageResponse;
import com.thelazydaniel.taskflow.user.dto.response.UserAdminResponse;
import com.thelazydaniel.taskflow.user.dto.response.UserResponse;
import com.thelazydaniel.taskflow.user.dto.response.UserSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.findSelf());
    }

    @PutMapping(value = "/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        long id = SecurityUtils.getCurrentUserId();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.updateSelf(updateUserRequest));
    }

    @GetMapping(value = "/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable(value = "id") long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.findUserById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<UserSummaryResponse>> getAllUsers(
            @Valid PageRequest pageRequest) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getAllUsers(pageRequest));
    }

    @PutMapping(value = "/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable long id,
            @Valid @RequestBody UpdateUserRoleRequest updateUserRoleRequest) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.updateUserRole(id, updateUserRoleRequest));
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUserById(
            @PathVariable long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }




}
