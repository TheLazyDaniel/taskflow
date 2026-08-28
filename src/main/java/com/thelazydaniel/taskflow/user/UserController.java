package com.thelazydaniel.taskflow.user;


import com.thelazydaniel.taskflow.common.dto.request.PageRequest;
import com.thelazydaniel.taskflow.user.dto.request.UpdateUserRequest;
import com.thelazydaniel.taskflow.user.dto.request.UpdateUserRoleRequest;
import com.thelazydaniel.taskflow.common.dto.response.PageResponse;
import com.thelazydaniel.taskflow.user.dto.response.UserResponse;
import com.thelazydaniel.taskflow.user.dto.response.UserSummaryResponse;
import com.thelazydaniel.taskflow.user.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser() {
        log.debug("Fetching current user");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.findSelf());
    }

    @PutMapping(value = "/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        log.info("Updating current user");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.updateSelf(updateUserRequest));
    }

    @GetMapping(value = "/{id}")
    @PreAuthorize("hasPermission(#id,'USER','READ')")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable(value = "id") long id) {
        log.debug("Fetching user: id={}", id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.findUserById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<UserSummaryResponse>> getAllUsers(
            @Valid @RequestBody PageRequest pageRequest) {
        log.debug("Listing users: page={}, size={}", pageRequest.page(), pageRequest.size());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getAllUsers(pageRequest));
    }

    @PutMapping(value = "/{id}/role")
    @PreAuthorize("hasPermission(#id,'USER','UPDATE')")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable long id,
            @Valid @RequestBody UpdateUserRoleRequest updateUserRoleRequest) {
        log.info("Updating user role: id={}, role={}", id, updateUserRoleRequest.role());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.updateUserRole(id, updateUserRoleRequest));
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasPermission(#id,'USER','DELETE')")
    public ResponseEntity<Void> deleteUserById(
            @PathVariable long id) {
        log.info("Deleting user: id={}", id);
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

}
