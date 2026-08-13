package com.thelazydaniel.taskflow.auth.controller;

import com.thelazydaniel.taskflow.auth.dto.request.LoginRequest;
import com.thelazydaniel.taskflow.auth.dto.response.JwtResponse;
import com.thelazydaniel.taskflow.auth.service.AuthService;
import com.thelazydaniel.taskflow.user.UserService;
import com.thelazydaniel.taskflow.auth.dto.request.RegisterRequest;
import com.thelazydaniel.taskflow.user.dto.response.UserResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping(value = "/users/register")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody RegisterRequest registerRequest) {

        log.info("Received request to register user: username={}, email={}"
                ,registerRequest.username(), registerRequest.email());

        UserResponse userResponse = userService.registerUser(registerRequest);

        log.info("User {} registered successfully", userResponse);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userResponse);
    }

    @PostMapping(value = "/users/login")
    public ResponseEntity<JwtResponse> userLogin(
            @Valid @RequestBody LoginRequest loginRequest) {

        log.info("Received request to login user: username={}", loginRequest.username());

        JwtResponse jwtResponse = authService.authenticateUser(
                loginRequest.username(),
                loginRequest.password()
        );
        return ResponseEntity.ok(jwtResponse);
    }
}
