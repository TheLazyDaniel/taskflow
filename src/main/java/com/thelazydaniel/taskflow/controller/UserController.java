package com.thelazydaniel.taskflow.controller;


import com.thelazydaniel.taskflow.dto.request.CreateUserRequest;
import com.thelazydaniel.taskflow.dto.request.PageRequest;
import com.thelazydaniel.taskflow.dto.request.UpdateUserRequest;
import com.thelazydaniel.taskflow.dto.request.UpdateUserRoleRequest;
import com.thelazydaniel.taskflow.dto.response.PageResponse;
import com.thelazydaniel.taskflow.dto.response.UserAdminResponse;
import com.thelazydaniel.taskflow.dto.response.UserResponse;
import com.thelazydaniel.taskflow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/register")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest createUserRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.registerUser(createUserRequest));
    }

    @GetMapping(value = "/me")
    public ResponseEntity<?> getCurrentUser() {
        Long id = 1L;
        //replace it with user id later
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.findUserById(id, "USER"));
        //Rewrite later
    }

    @PutMapping(value = "/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        Long id = 1L;
        //replace it with user id later
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.updateUser(updateUserRequest,id));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<?> getUserById(
            @PathVariable(value = "id") long id) {
        //replace it with user id later
        String userRole = "USER";
        //replace it with user role later
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.findUserById(id, "USER"));
    }

    @GetMapping
    public ResponseEntity<PageResponse<UserAdminResponse>> getAllUsers(
            @Valid PageRequest pageRequest) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getAllUsers(pageRequest));
    }

    @PutMapping(value = "/{id}/role")
    public ResponseEntity<UserAdminResponse> updateUserRole(
            @PathVariable long id,
            @Valid @RequestBody UpdateUserRoleRequest updateUserRoleRequest) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.updateUserRole(id, updateUserRoleRequest));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteUserById(
            @PathVariable long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }




}
