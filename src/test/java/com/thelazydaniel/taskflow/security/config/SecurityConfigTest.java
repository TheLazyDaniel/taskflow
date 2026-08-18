package com.thelazydaniel.taskflow.security.config;

import com.thelazydaniel.taskflow.security.entry.JwtAuthEntryPoint;
import com.thelazydaniel.taskflow.security.filter.JwtAuthenticationFilter;
import com.thelazydaniel.taskflow.user.UserController;
import com.thelazydaniel.taskflow.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Qualifier("SecurityFilterChain")
    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Value("${spring.mvc.servlet.path:}")
    private String servletPath;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtAuthEntryPoint jwtAuthEntryPoint;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private DaoAuthenticationProvider authenticationProvider;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private String buildPath(String path) {
        return servletPath + path;
    }

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
        assertThat(securityFilterChain).isNotNull();
    }

    @Test
    void protectedEndpoints_ShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get(buildPath("/users")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminUser_ShouldAccessAllUsers() throws Exception {
        mockMvc.perform(get(buildPath("/users")))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userWithoutAdminRole_ShouldNotAccessAllUsers() throws Exception {
        mockMvc.perform(get(buildPath("/users")))
                .andExpect(status().isForbidden());
    }
}