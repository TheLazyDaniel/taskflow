package com.thelazydaniel.taskflow.auth.service;

import com.thelazydaniel.taskflow.auth.dto.request.LogoutRequest;
import com.thelazydaniel.taskflow.auth.dto.response.LogoutResponse;
import com.thelazydaniel.taskflow.auth.exception.*;
import com.thelazydaniel.taskflow.security.TokenType;
import com.thelazydaniel.taskflow.security.TokenValidationResult;
import com.thelazydaniel.taskflow.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutService {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlackListService tokenBlackListService;

    @Transactional
    public LogoutResponse logout(HttpServletRequest request, LogoutRequest logoutRequest) {
        String username = null;

        try {
            // Extract and validate access token
            String accessToken = extractAccessToken(request);

            // Extract and validate refresh token
            String refreshToken = extractRefreshToken(logoutRequest);

            // Validate both tokens
            validateTokens(accessToken, refreshToken);

            // Extract username and verify tokens match
            username = verifyTokenOwnership(accessToken, refreshToken);

            // Blacklist both tokens
            blacklistTokens(accessToken, refreshToken);

            log.info("User logged out successfully: {}", username);

            return LogoutResponse.builder()
                    .username(username)
                    .message("Logout successful")
                    .timestamp(LocalDateTime.now())
                    .success(true)
                    .build();

        } catch (UnauthorizedException | InvalidAccessTokenException |
                 InvalidRefreshTokenException | TokenNotMatchException e) {
            // Re-throw specific exceptions for proper handling
            log.warn("Logout failed for user {}: {}", username, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during logout for user {}: {}", username, e.getMessage(), e);
            throw new LogoutException("Failed to process logout request",e);
        } finally {
            // Always clear security context, even if logout fails
            SecurityContextHolder.clearContext();
        }
    }

    private String extractAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken == null || bearerToken.isEmpty()) {
            throw new UnauthorizedException("Authorization header is missing");
        }

        if (!bearerToken.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid Authorization header format");
        }

        String accessToken = bearerToken.substring(7);
        if (accessToken.isEmpty()) {
            throw new UnauthorizedException("Access token is empty");
        }

        return accessToken;
    }

    private String extractRefreshToken(LogoutRequest logoutRequest) {
        if (logoutRequest == null) {
            throw new InvalidRefreshTokenException("Logout request body is required");
        }

        String refreshToken = logoutRequest.refreshToken();
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new InvalidRefreshTokenException("Refresh token is required");
        }

        return refreshToken;
    }

    private void validateTokens(String accessToken, String refreshToken) {
        // Validate refresh token first (more critical for logout)
        TokenValidationResult refreshValidation =
                jwtTokenProvider.validateToken(refreshToken, TokenType.REFRESH);

        if (!refreshValidation.isValid()) {
            if (refreshValidation.isExpired()) {
                throw new RefreshTokenExpiredException("Refresh token has expired");
            }
            throw new InvalidRefreshTokenException(refreshValidation.message());
        }

        // Validate access token
        TokenValidationResult accessValidation =
                jwtTokenProvider.validateToken(accessToken, TokenType.ACCESS);

        if (!accessValidation.isValid()) {
            if (accessValidation.isExpired()) {
                // Access token expired but refresh token valid - allow logout
                log.warn("Access token expired during logout, proceeding with refresh token");
            } else {
                throw new InvalidAccessTokenException(accessValidation.message());
            }
        }
    }

    private String verifyTokenOwnership(String accessToken, String refreshToken) {
        String usernameFromAccessToken;
        String usernameFromRefreshToken;

        try {
            usernameFromAccessToken = jwtTokenProvider.getUsernameFromToken(accessToken);
        } catch (Exception e) {
            throw new InvalidAccessTokenException("Cannot extract username from access token");
        }

        try {
            usernameFromRefreshToken = jwtTokenProvider.getUsernameFromRefreshToken(refreshToken);
        } catch (Exception e) {
            throw new InvalidRefreshTokenException("Cannot extract username from refresh token");
        }

        if (usernameFromAccessToken == null || usernameFromRefreshToken == null) {
            throw new TokenNotMatchException("Cannot verify token ownership");
        }

        if (!usernameFromAccessToken.equals(usernameFromRefreshToken)) {
            log.warn("Token mismatch: access={}, refresh={}",
                    usernameFromAccessToken, usernameFromRefreshToken);
            throw new TokenNotMatchException("Access token and refresh token do not match");
        }

        return usernameFromAccessToken;
    }

    private void blacklistTokens(String accessToken, String refreshToken) {
        boolean accessTokenBlacklisted = false;
        boolean refreshTokenBlacklisted = false;

        try {
            long accessTokenRemainingMs =
                    jwtTokenProvider.getRemainingExpirationMsFromAccessToken(accessToken);

            if (accessTokenRemainingMs > 0) {
                tokenBlackListService.blacklistToken(accessToken, accessTokenRemainingMs);
                accessTokenBlacklisted = true;
                log.debug("Access token blacklisted");
            }
        } catch (Exception e) {
            log.error("Failed to blacklist access token: {}", e.getMessage());
        }

        try {
            long refreshTokenRemainingMs =
                    jwtTokenProvider.getRemainingExpirationMsFromRefreshToken(refreshToken);

            if (refreshTokenRemainingMs > 0) {
                tokenBlackListService.blacklistToken(refreshToken, refreshTokenRemainingMs);
                refreshTokenBlacklisted = true;
                log.debug("Refresh token blacklisted");
            }
        } catch (Exception e) {
            log.error("Failed to blacklist refresh token: {}", e.getMessage());
        }

        // If both failed, throw exception
        if (!accessTokenBlacklisted && !refreshTokenBlacklisted) {
            throw new LogoutException("Failed to blacklist tokens" );
        }

        // Log partial success
        if (!accessTokenBlacklisted || !refreshTokenBlacklisted) {
            log.warn("Partial token blacklisting - Access: {}, Refresh: {}",
                    accessTokenBlacklisted, refreshTokenBlacklisted);
        }
    }
}