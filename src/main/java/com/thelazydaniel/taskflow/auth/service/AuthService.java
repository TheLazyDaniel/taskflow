package com.thelazydaniel.taskflow.auth.service;

import com.thelazydaniel.taskflow.auth.dto.response.JwtResponse;
import com.thelazydaniel.taskflow.security.jwt.JwtTokenProvider;
import com.thelazydaniel.taskflow.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public JwtResponse authenticateUser(String username, String password) {

        log.debug("Authenticating user {}", username);

        long startTime = System.currentTimeMillis();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        log.debug("Authenticated user {}", authentication.getPrincipal());

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        log.debug("Roles {}", roles);

        String accessToken = jwtTokenProvider.generateTokenFromUsername(
                userDetails.getUsername(),
                roles
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(
                userDetails.getUsername()
        );

        long duration = System.currentTimeMillis() - startTime;
        log.info("User authenticated: {}, roles: {}, duration: {}ms", username, roles, duration);

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .type("Bearer")
                .username(userDetails.getUsername())
                .roles(roles)
                .build();
    }
}
