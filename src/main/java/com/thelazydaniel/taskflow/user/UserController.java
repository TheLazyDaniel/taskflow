package com.thelazydaniel.taskflow.user;


import com.thelazydaniel.taskflow.common.util.SecurityUtils;
import com.thelazydaniel.taskflow.common.dto.request.PageRequest;
import com.thelazydaniel.taskflow.user.dto.request.UpdateUserRequest;
import com.thelazydaniel.taskflow.user.dto.request.UpdateUserRoleRequest;
import com.thelazydaniel.taskflow.common.dto.response.PageResponse;
import com.thelazydaniel.taskflow.user.dto.response.UserAdminResponse;
import com.thelazydaniel.taskflow.user.dto.response.UserResponse;
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
    public ResponseEntity<?> getCurrentUser() {
        long id = SecurityUtils.getCurrentUserId();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.findUserById(id));
    }

    @PutMapping(value = "/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        long id = SecurityUtils.getCurrentUserId();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.updateUser(updateUserRequest,id));
    }

    @GetMapping(value = "/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(
            @PathVariable(value = "id") long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.findUserById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<UserAdminResponse>> getAllUsers(
            @Valid PageRequest pageRequest) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getAllUsers(pageRequest));
    }

    @PutMapping(value = "/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserAdminResponse> updateUserRole(
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
