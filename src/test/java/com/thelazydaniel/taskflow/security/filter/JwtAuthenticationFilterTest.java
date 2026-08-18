package com.thelazydaniel.taskflow.security.filter;

import com.thelazydaniel.taskflow.security.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private String validToken;
    private String username;
    private List<String> roles;

    @BeforeEach
    void setUp() {
        username = "testuser";
        roles = List.of("ROLE_USER");
        validToken = "valid-jwt-token";

        // Clear security context before each test
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_ShouldSetAuthentication_WhenValidTokenProvided() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(validToken)).thenReturn(username);
        when(jwtTokenProvider.getRolesFromToken(validToken)).thenReturn(roles);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo(username);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .hasSize(1);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldNotSetAuthentication_WhenNoToken() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    @Test
    void doFilterInternal_ShouldNotSetAuthentication_WhenInvalidToken() throws Exception {
        // Arrange
        String invalidToken = "invalid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).getUsernameFromToken(anyString());
    }

    @Test
    void doFilterInternal_ShouldSendErrorResponse_WhenExceptionOccurs() throws Exception {
        PrintWriter printWriter = mock(PrintWriter.class);
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");
        when(jwtTokenProvider.validateToken(validToken))
                .thenThrow(new RuntimeException("Token validation failed"));
        when(response.getWriter()).thenReturn(printWriter);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldSkipTokenValidation_ForWhitelistedPaths() throws Exception {
        // You can add this if you have whitelisted paths
        // This test assumes you might add whitelist functionality later
        when(request.getRequestURI()).thenReturn("/users/login");
        when(request.getMethod()).thenReturn("POST");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    @Test
    void parseJwt_ShouldExtractToken_WhenValidHeader() {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        // Act - using reflection to test private method
        // Or you can make parseJwt package-private for testing
        // For now, we'll test through doFilterInternal
        try {
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        } catch (Exception e) {
            // Ignore
        }

        // Assert
        verify(jwtTokenProvider).validateToken(validToken);
    }

    @Test
    void parseJwt_ShouldReturnNull_WhenNoAuthorizationHeader() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");

        // Act
        try {
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        } catch (Exception e) {
            // Ignore
        }

        // Assert
        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void parseJwt_ShouldReturnNull_WhenAuthorizationHeaderDoesNotStartWithBearer() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Basic " + validToken);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");

        // Act
        try {
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        } catch (Exception e) {
            // Ignore
        }

        // Assert
        verify(jwtTokenProvider, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);
    }
}