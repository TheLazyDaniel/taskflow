package com.thelazydaniel.taskflow.auth.service;

import com.thelazydaniel.taskflow.auth.dto.request.RefreshTokenRequest;
import com.thelazydaniel.taskflow.auth.dto.request.TokenVerifyRequest;
import com.thelazydaniel.taskflow.auth.dto.response.JwtResponse;
import com.thelazydaniel.taskflow.auth.dto.response.TokenRefreshResponse;
import com.thelazydaniel.taskflow.auth.dto.response.TokenVerifyResponse;
import com.thelazydaniel.taskflow.auth.exception.InvalidRefreshTokenException;
import com.thelazydaniel.taskflow.auth.exception.RefreshTokenExpiredException;
import com.thelazydaniel.taskflow.security.TokenType;
import com.thelazydaniel.taskflow.security.TokenValidationResult;
import com.thelazydaniel.taskflow.security.jwt.JwtTokenProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider jwtTokenProvider;

    private final TokenBlackListService tokenBlackListService;

    private final LoginAttemptService loginAttemptService;

    public JwtResponse authenticateUser(String username, String password, String ipAddress) {
        if (loginAttemptService.isAccountLocked(username)){
            throw new LockedException("Account is locked due to too many login attempts. Try again later.");
        }
        if (loginAttemptService.isIpBlocked(ipAddress)){
            throw new LockedException("Account is locked due to too many login attempts. Try again later.");
        }
        try {
            log.debug("Authenticating user {}", username);

            long startTime = System.currentTimeMillis();

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            loginAttemptService.loginSucceeded(username,ipAddress);

            log.debug("Authenticated user {}", authentication.getPrincipal());

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            List<String> roles = extractRoles(userDetails);

            log.debug("Roles {}", roles);

            String accessToken = jwtTokenProvider.generateTokenFromUsername(
                    userDetails.getUsername(),
                    roles
            );

            String refreshToken = jwtTokenProvider.generateRefreshToken(
                    userDetails.getUsername(),
                    roles
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

        } catch (AccountStatusException e) {
            String statusMessage = getMessage(e);
            log.warn("{} for user: {}", statusMessage, username);
            loginAttemptService.loginFailed(username, ipAddress);
            throw e;

        } catch (BadCredentialsException e) {
            log.warn("Invalid username or password: {}", username);
            loginAttemptService.loginFailed(username, ipAddress);
            throw e;

        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", username, e);
            loginAttemptService.loginFailed(username, ipAddress);
            throw e;
        }
    }

    private static @NonNull String getMessage(AccountStatusException e) {

        return switch (e) {
            case DisabledException disabledException -> "Account disabled";
            case LockedException lockedException -> "Account locked";
            case AccountExpiredException accountExpiredException -> "Account expired";
            case CredentialsExpiredException credentialsExpiredException -> "Credentials expired";
            case null, default -> "Account status issue";
        };
    }

    public TokenRefreshResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.refreshToken();

        // Check if token is blacklisted
        if (tokenBlackListService.isTokenBlacklisted(refreshToken)) {
            throw new InvalidRefreshTokenException("Refresh token has been revoked");
        }

        TokenValidationResult validationResult = jwtTokenProvider.validateToken(refreshToken, TokenType.REFRESH);

        if (validationResult.isExpired()) {
            throw new RefreshTokenExpiredException(validationResult.message());
        }

        if (!validationResult.isValid()) {
            throw new InvalidRefreshTokenException(validationResult.message());
        }

        try {
            // Extract claims using refresh token methods
            String username = jwtTokenProvider.getUsernameFromRefreshToken(refreshToken);
            List<String> roles = jwtTokenProvider.getRolesFromRefreshToken(refreshToken);

            // Generate new tokens
            String newAccessToken = jwtTokenProvider.generateTokenFromUsername(username, roles);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(username, roles);

            // Blacklist the old refresh token for rotation
            tokenBlackListService.blacklistToken(refreshToken,
                    jwtTokenProvider.getRemainingExpirationMsFromRefreshToken(refreshToken));

            log.info("Token refreshed successfully for user: {}", username);

            return new TokenRefreshResponse(newAccessToken, newRefreshToken, "Bearer");

        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage());
            throw new InvalidRefreshTokenException("Failed to refresh token: " + e.getMessage());
        }
    }

    public TokenVerifyResponse verifyToken(TokenVerifyRequest tokenVerifyRequest){
        String token = tokenVerifyRequest.token();
        TokenType tokenType = "ASSESS".equalsIgnoreCase(String.valueOf(tokenVerifyRequest.type()))
                ? TokenType.ACCESS
                : TokenType.REFRESH;

        boolean isRevoked = tokenBlackListService.isTokenBlacklisted(token);
        if (isRevoked) {
            log.warn("Token has been revoked");
            return TokenVerifyResponse.builder()
                    .valid(false)
                    .expired(false)
                    .revoked(true)
                    .message("Token has been revoked")
                    .tokenType(tokenType.name())
                    .build();
        }

        TokenValidationResult validationResult = jwtTokenProvider.validateToken(token,tokenType);
        if (!validationResult.isValid()) {
            log.warn("Invalid token: {}", validationResult.message());
            return TokenVerifyResponse.builder()
                    .valid(false)
                    .expired(validationResult.isExpired())
                    .revoked(false)
                    .message(validationResult.message())
                    .tokenType(tokenType.name())
                    .remainingExpirationMs(validationResult.remainingExpirationMs())
                    .build();
        }
        try {
            String username = extractUsername(token, tokenType);
            List<String> roles = extractRoles(token, tokenType);
            long remainingMs = getRemainingExpirationMs(token, tokenType);
            Date expirationDate = getExpirationDate(token, tokenType);

            log.info("Token verified successfully for user: {}, type: {}, remaining: {}ms",
                    username, tokenType, remainingMs);

            return TokenVerifyResponse.builder()
                    .valid(true)
                    .expired(false)
                    .revoked(false)
                    .message("Token is valid")
                    .tokenType(tokenType.name())
                    .username(username)
                    .roles(roles)
                    .remainingExpirationMs(remainingMs)
                    .expirationDate(expirationDate)
                    .build();

        } catch (Exception e) {
            log.error("Error extracting token information: {}", e.getMessage());
            return TokenVerifyResponse.builder()
                    .valid(false)
                    .expired(false)
                    .revoked(false)
                    .message("Error extracting token information: " + e.getMessage())
                    .tokenType(tokenType.name())
                    .build();
        }
    }

    private List<String> extractRoles(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)  // Filter out null authority strings
                .map(String::trim)  // Filter out empty strings
                .filter(trim -> !trim.isEmpty())  // Trim whitespace
                .distinct()  // Remove duplicates
                .collect(Collectors.toList());
    }

    private List<String> extractRoles(String token, TokenType tokenType) {
        return tokenType == TokenType.REFRESH
                ? jwtTokenProvider.getRolesFromRefreshToken(token)
                : jwtTokenProvider.getRolesFromToken(token);
    }

    private String extractUsername(String token, TokenType tokenType) {
        return tokenType == TokenType.REFRESH
                ? jwtTokenProvider.getUsernameFromRefreshToken(token)
                : jwtTokenProvider.getUsernameFromToken(token);
    }

    private long getRemainingExpirationMs(String token, TokenType tokenType) {
        return tokenType == TokenType.REFRESH
                ? jwtTokenProvider.getRemainingExpirationMsFromRefreshToken(token)
                : jwtTokenProvider.getRemainingExpirationMsFromAccessToken(token);
    }

    private Date getExpirationDate(String token, TokenType tokenType) {
        return tokenType == TokenType.REFRESH
                ? jwtTokenProvider.getExpirationFromRefreshToken(token)
                : jwtTokenProvider.getExpirationFromAccessToken(token);
    }
}
