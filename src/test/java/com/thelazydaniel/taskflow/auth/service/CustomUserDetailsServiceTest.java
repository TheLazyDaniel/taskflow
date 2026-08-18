package com.thelazydaniel.taskflow.auth.service;

import com.thelazydaniel.taskflow.auth.entity.SecurityUser;
import com.thelazydaniel.taskflow.auth.exception.AccountDisabledException;
import com.thelazydaniel.taskflow.auth.exception.AccountLockedException;
import com.thelazydaniel.taskflow.user.UserRepository;
import com.thelazydaniel.taskflow.user.entity.User;
import com.thelazydaniel.taskflow.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User user;
    private String username;

    @BeforeEach
    void setUp() {
        username = "testuser";

        user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail("test@email.com");
        user.setPasswordHash("encodedPassword");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        user.setAccountNonLocked(true);
    }

    @Test
    void loadUserByUsername_ShouldReturnSecurityUser_WhenUserExistsAndEnabled() {
        // Arrange
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(SecurityUser.class);
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority())
                .isEqualTo("ROLE_USER");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.isAccountNonLocked()).isTrue();
        assertThat(result.isAccountNonExpired()).isTrue();
        assertThat(result.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void loadUserByUsername_ShouldThrowUsernameNotFoundException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(username);
        });
    }

    @Test
    void loadUserByUsername_ShouldThrowAccountDisabledException_WhenUserDisabled() {
        // Arrange
        user.setEnabled(false);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(AccountDisabledException.class, () -> {
            customUserDetailsService.loadUserByUsername(username);
        });
    }

    @Test
    void loadUserByUsername_ShouldThrowAccountLockedException_WhenUserLocked() {
        // Arrange
        user.setAccountNonLocked(false);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(AccountLockedException.class, () -> {
            customUserDetailsService.loadUserByUsername(username);
        });
    }

    @Test
    void loadUserByUsername_ShouldHandleAdminRole() {
        // Arrange
        user.setRole(UserRole.ADMIN);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAuthorities().iterator().next().getAuthority())
                .isEqualTo("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_ShouldHandleManagerRole() {
        // Arrange
        user.setRole(UserRole.MANAGER);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAuthorities().iterator().next().getAuthority())
                .isEqualTo("ROLE_MANAGER");
    }

    @Test
    void loadUserByUsername_ShouldReturnNonNullForValidUser() {
        // Arrange
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
    }
}