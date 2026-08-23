package com.thelazydaniel.taskflow.auth.controller;

import com.thelazydaniel.taskflow.auth.dto.request.LoginRequest;
import com.thelazydaniel.taskflow.auth.dto.request.RefreshTokenRequest;
import com.thelazydaniel.taskflow.auth.dto.response.JwtResponse;
import com.thelazydaniel.taskflow.auth.dto.response.TokenRefreshResponse;
import com.thelazydaniel.taskflow.auth.service.AuthService;
import com.thelazydaniel.taskflow.common.util.SecurityUtils;
import com.thelazydaniel.taskflow.user.service.UserService;
import com.thelazydaniel.taskflow.auth.dto.request.RegisterRequest;
import com.thelazydaniel.taskflow.user.dto.response.UserPublicResponse;
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
    public ResponseEntity<UserPublicResponse> createUser(
            @Valid @RequestBody RegisterRequest registerRequest) {

        log.info("Received request to register user: username={}, email={}"
                ,registerRequest.username(), registerRequest.email());

        UserPublicResponse userPublicResponse = userService.registerUser(registerRequest);

        log.info("User {} registered successfully", userPublicResponse);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userPublicResponse);
    }

    @PostMapping(value = "/auth/login")
    public ResponseEntity<JwtResponse> userLogin(
            @Valid @RequestBody LoginRequest loginRequest) {

        log.info("Received request to login user: username={}", loginRequest.username());

        return ResponseEntity.ok(authService.authenticateUser(
                loginRequest.username(),
                loginRequest.password()
        ));
    }

    @PostMapping(value = "/auth/refresh")
    public ResponseEntity<TokenRefreshResponse> userRefresh(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        log.info("Received request to refresh token: token={}", refreshTokenRequest.refreshToken());
        return ResponseEntity.ok(authService.refreshToken(refreshTokenRequest));
    }

    @PostMapping(value = "/auth/logout")
    public ResponseEntity<String> userLogout() {

        log.info("Received request to logout user: username={}", SecurityUtils.getCurrentUsername());

        return ResponseEntity.ok(authService.userLogout());
    }

    @PostMapping(value = "/auth/verify")
    public ResponseEntity<JwtResponse> userVerify(
            @Valid @RequestBody LoginRequest loginRequest) {

        log.info("Received request to login user: username={}", loginRequest.username());

        JwtResponse jwtResponse = authService.authenticateUser(
                loginRequest.username(),
                loginRequest.password()
        );
        return ResponseEntity.ok(jwtResponse);
    }
}
