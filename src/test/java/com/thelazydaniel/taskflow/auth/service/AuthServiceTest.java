package com.thelazydaniel.taskflow.auth.service;

import com.thelazydaniel.taskflow.auth.dto.response.JwtResponse;
import com.thelazydaniel.taskflow.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private UserDetails userDetails;
    private List<String> roles;
    private String username;
    private String password;

    @BeforeEach
    void setUp() {
        username = "testuser";
        password = "password123";
        roles = List.of("ROLE_USER");

        userDetails = User.builder()
                .username(username)
                .password("encodedPassword")
                .authorities(roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList())
                .build();
    }

    @Test
    void authenticateUser_ShouldReturnJwtResponse_WhenCredentialsValid() {
        // Arrange
        String expectedAccessToken = "access-token-123";
        String expectedRefreshToken = "refresh-token-456";

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateTokenFromUsername(username, roles))
                .thenReturn(expectedAccessToken);
        when(jwtTokenProvider.generateRefreshToken(username))
                .thenReturn(expectedRefreshToken);

        // Act
        JwtResponse response = authService.authenticateUser(username, password);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(expectedAccessToken);
        assertThat(response.getRefreshToken()).isEqualTo(expectedRefreshToken);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getRoles()).containsExactly("ROLE_USER");
        assertThat(response.getType()).isEqualTo("Bearer");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateTokenFromUsername(username, roles);
        verify(jwtTokenProvider).generateRefreshToken(username);
    }

    @Test
    void authenticateUser_ShouldThrowException_WhenCredentialsInvalid() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authService.authenticateUser(username, password);
        });

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider, never()).generateTokenFromUsername(anyString(), anyList());
        verify(jwtTokenProvider, never()).generateRefreshToken(anyString());
    }

    @Test
    void authenticateUser_ShouldLogAuthenticationDetails() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateTokenFromUsername(username, roles))
                .thenReturn("token");
        when(jwtTokenProvider.generateRefreshToken(username))
                .thenReturn("refresh");

        // Act
        authService.authenticateUser(username, password);

        // Assert - Verify interactions
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateTokenFromUsername(username, roles);
        verify(jwtTokenProvider).generateRefreshToken(username);
    }
}