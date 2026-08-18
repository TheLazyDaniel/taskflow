package com.thelazydaniel.taskflow.common.config;

import com.thelazydaniel.taskflow.auth.entity.SecurityUser;
import com.thelazydaniel.taskflow.user.entity.User;
import com.thelazydaniel.taskflow.user.enums.UserRole;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public UserDetailsService testUserDetailsService() {
        // Create test users
        User user = createUser(1L, "testuser", UserRole.USER);
        User admin = createUser(2L, "admin", UserRole.ADMIN);
        User manager = createUser(3L, "manager", UserRole.MANAGER);

        return new InMemoryUserDetailsManager(
                new SecurityUser(user),
                new SecurityUser(admin),
                new SecurityUser(manager)
        );
    }

    private User createUser(Long id, String username, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@email.com");
        user.setPasswordHash("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG");
        user.setRole(role);
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        return user;
    }
}