package com.thelazydaniel.taskflow.security.config;

import com.thelazydaniel.taskflow.auth.entity.SecurityUser;

import com.thelazydaniel.taskflow.security.entry.JwtAuthEntryPoint;

import com.thelazydaniel.taskflow.user.entity.User;
import com.thelazydaniel.taskflow.user.enums.UserRole;
import com.thelazydaniel.taskflow.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private UserService userService;

    private PasswordEncoder passwordEncoder;
    private User testUser;
    private SecurityUser securityUser;
    private String rawPassword;

    @BeforeEach
    void setUp() {
        // ✅ Use real BCrypt encoder for the test
        passwordEncoder = new BCryptPasswordEncoder();
        rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Create test user with proper encoded password
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@email.com");
        testUser.setPasswordHash(encodedPassword);  // ✅ Properly encoded
        testUser.setRole(UserRole.USER);
        testUser.setEnabled(true);
        testUser.setAccountNonLocked(true);

        // Create SecurityUser from test user
        securityUser = new SecurityUser(testUser);

        // ✅ Mock UserDetailsService to return the SecurityUser
        when(userDetailsService.loadUserByUsername("testuser"))
                .thenReturn(securityUser);
    }

    // ============================================
    // ✅ TEST: Bean Creation
    // ============================================

    @Test
    void securityFilterChain_ShouldBeCreated() {
        SecurityFilterChain filterChain = applicationContext.getBean(SecurityFilterChain.class);
        assertThat(filterChain).isNotNull();
    }

    @Test
    void authenticationManager_ShouldBeCreated() {
        org.springframework.security.authentication.AuthenticationManager authenticationManager =
                applicationContext.getBean(org.springframework.security.authentication.AuthenticationManager.class);
        assertThat(authenticationManager).isNotNull();
    }

    @Test
    void authenticationProvider_ShouldBeCreated() {
        org.springframework.security.authentication.AuthenticationProvider authenticationProvider =
                applicationContext.getBean(org.springframework.security.authentication.AuthenticationProvider.class);
        assertThat(authenticationProvider).isNotNull();
    }

    @Test
    void passwordEncoder_ShouldBeCreated() {
        PasswordEncoder passwordEncoder = applicationContext.getBean(PasswordEncoder.class);
        assertThat(passwordEncoder).isNotNull();
    }

    // ============================================
    // ✅ TEST: Public Endpoints (Should be accessible)
    // ============================================

    @Test
    void permitAll_ShouldAllowAccessToRegisterEndpoint() throws Exception {
        String jsonRequest = """
                {
                    "username": "testuser",
                    "email": "test@email.com",
                    "password": "password123",
                    "firstName": "Test",
                    "lastName": "User"
                }
                """;

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated());
    }

    @Test
    void permitAll_ShouldAllowAccessToLoginEndpoint() throws Exception {
        String jsonRequest = """
                {
                    "username": "testuser",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());
    }

    // ============================================
    // ✅ TEST: Protected Endpoints (Should require authentication)
    // ============================================

    @Test
    void authenticated_ShouldBlockAccessToProtectedEndpoint_WhenNoAuthentication() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticated_ShouldBlockAccessToAdminEndpoint_WhenNoAuthentication() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticated_ShouldBlockAccessToUserByIdEndpoint_WhenNoAuthentication() throws Exception {
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticated_ShouldBlockAccessToUpdateRoleEndpoint_WhenNoAuthentication() throws Exception {
        mockMvc.perform(put("/users/1/role"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticated_ShouldBlockAccessToDeleteUserEndpoint_WhenNoAuthentication() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticated_ShouldBlockAccessToUpdateCurrentUserEndpoint_WhenNoAuthentication() throws Exception {
        mockMvc.perform(put("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    // ============================================
    // ✅ TEST: Logout Configuration
    // ============================================

    @Test
    void logoutEndpoint_ShouldRequireAuthentication() throws Exception {
        mockMvc.perform(post("/logout"))
                .andExpect(status().is3xxRedirection());
    }

    // ============================================
    // ✅ TEST: CSRF Configuration
    // ============================================

    @Test
    void csrf_ShouldBeDisabled() throws Exception {
        mockMvc.perform(post("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    // ============================================
    // ✅ TEST: Exception Handling
    // ============================================

    @Test
    void authenticationEntryPoint_ShouldBeConfigured() {
        JwtAuthEntryPoint entryPoint = applicationContext.getBean(JwtAuthEntryPoint.class);
        assertThat(entryPoint).isNotNull();
    }

    // ============================================
    // ✅ TEST: Password Encoding
    // ============================================

    @Test
    void passwordEncoder_ShouldEncodeAndMatchPassword() {
        PasswordEncoder passwordEncoder = applicationContext.getBean(PasswordEncoder.class);
        String rawPassword = "testPassword123";

        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertThat(encodedPassword).isNotNull();
        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
        assertThat(passwordEncoder.matches("wrongPassword", encodedPassword)).isFalse();
    }
}