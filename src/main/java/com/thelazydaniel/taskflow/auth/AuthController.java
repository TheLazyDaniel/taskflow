package com.thelazydaniel.taskflow.auth;

import com.thelazydaniel.taskflow.auth.dto.request.*;
import com.thelazydaniel.taskflow.auth.dto.response.JwtResponse;
import com.thelazydaniel.taskflow.auth.dto.response.LogoutResponse;
import com.thelazydaniel.taskflow.auth.dto.response.TokenRefreshResponse;
import com.thelazydaniel.taskflow.auth.dto.response.TokenVerifyResponse;
import com.thelazydaniel.taskflow.auth.service.AuthService;
import com.thelazydaniel.taskflow.auth.service.LogoutService;
import com.thelazydaniel.taskflow.common.util.SecurityUtils;
import com.thelazydaniel.taskflow.user.service.UserService;
import com.thelazydaniel.taskflow.user.dto.response.UserPublicResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final LogoutService logoutService;

    public AuthController(AuthService authService, UserService userService, LogoutService logoutService) {
        this.authService = authService;
        this.userService = userService;
        this.logoutService = logoutService;
    }

    @PostMapping(value = "/register")
    public ResponseEntity<UserPublicResponse> createUser(
            @Valid @RequestBody RegisterRequest registerRequest) {

        log.info("Received request to register user: username={}, email={}"
                ,registerRequest.username(), registerRequest.email());

        UserPublicResponse userPublicResponse = userService.registerUser(registerRequest);

        log.info("User registered successfully: username={}", registerRequest.username());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userPublicResponse);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<JwtResponse> userLogin(
            @Valid @RequestBody LoginRequest loginRequest, HttpServletRequest httpServletRequest) {

        log.info("Received request to login user: username={}", loginRequest.username());

        return ResponseEntity.ok(authService.authenticateUser(
                loginRequest.username(),
                loginRequest.password(),
                getClientIP(httpServletRequest)
        ));
    }

    @PostMapping(value = "/refresh")
    public ResponseEntity<TokenRefreshResponse> userRefresh(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        log.info("Received request to refresh token");
        return ResponseEntity.ok(authService.refreshToken(refreshTokenRequest));
    }

    @PostMapping(value = "/logout")
    public ResponseEntity<LogoutResponse> userLogout(
            @Valid @RequestBody LogoutRequest logoutRequest,
            HttpServletRequest httpServletRequest
            ) {

        log.info("Received request to logout user: username={}", SecurityUtils.getCurrentUsername());

        return ResponseEntity.ok(logoutService.logout(httpServletRequest,logoutRequest));
    }

    @PostMapping(value = "/verify")
    public ResponseEntity<TokenVerifyResponse> userVerify(
            @Valid @RequestBody TokenVerifyRequest tokenVerifyRequest) {

        log.info("Received request to verify token, type {}",tokenVerifyRequest.type().name());

        return ResponseEntity.ok(authService.verifyToken(tokenVerifyRequest));
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
