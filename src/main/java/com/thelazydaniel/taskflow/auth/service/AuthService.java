package com.thelazydaniel.taskflow.auth.service;

import com.thelazydaniel.taskflow.auth.dto.request.RefreshTokenRequest;
import com.thelazydaniel.taskflow.auth.dto.request.TokenVerifyRequest;
import com.thelazydaniel.taskflow.auth.dto.response.JwtResponse;
import com.thelazydaniel.taskflow.auth.dto.response.TokenRefreshResponse;
import com.thelazydaniel.taskflow.auth.dto.response.TokenVerifyResponse;
import com.thelazydaniel.taskflow.auth.exception.AccountDisabledException;
import com.thelazydaniel.taskflow.auth.exception.AccountLockedException;
import com.thelazydaniel.taskflow.auth.exception.InvalidRefreshTokenException;
import com.thelazydaniel.taskflow.auth.exception.RefreshTokenExpiredException;
import com.thelazydaniel.taskflow.security.jwt.JwtTokenProvider;
import com.thelazydaniel.taskflow.user.service.UserAuthService;
import com.thelazydaniel.taskflow.user.service.UserValidationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider jwtTokenProvider;

    private final UserAuthService userAuthService;


    public JwtResponse authenticateUser(String username, String password) {
        try {
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

        } catch (BadCredentialsException e) {
            // This will catch UsernameNotFoundException when hideUserNotFoundExceptions=true
            log.warn("Invalid credentials for user: {}", username);
            throw new BadCredentialsException("Invalid username or password", e);

        } catch (AccountDisabledException e) {
            log.warn("Account disabled for user: {}", username);
            throw new BadCredentialsException("Account is disabled", e);

        } catch (AccountLockedException e) {
            log.warn("Account locked for user: {}", username);
            throw new BadCredentialsException("Account is locked", e);

        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", username, e);
            throw new BadCredentialsException("Authentication failed", e);
        }
    }

    public TokenRefreshResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.refreshToken();
        if (jwtTokenProvider.isTokenExpired(refreshToken)){
            throw new RefreshTokenExpiredException("Refresh token has expired. Please login again.");
        }
        if (!jwtTokenProvider.validateToken(refreshToken)){
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }
        try {
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            List<String> roles = userAuthService.getUserRolesFromUsername(username);
            String newAccessToken = jwtTokenProvider.generateTokenFromUsername(
                    username,
                    roles
            );
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);
            return new TokenRefreshResponse(newAccessToken, newRefreshToken, "Bearer");
        } catch (JwtException | IllegalArgumentException e){
            throw new InvalidRefreshTokenException("Invalid refresh token: " + e.getMessage());
        }
    }

    public TokenVerifyResponse verifyToken(TokenVerifyRequest tokenVerifyRequest){
        return null;
    }

    public String userLogout(){
        SecurityContextHolder.clearContext();
        return "logout successful";
    }
}
