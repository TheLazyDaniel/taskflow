package com.thelazydaniel.taskflow.user;

import com.thelazydaniel.taskflow.auth.entity.SecurityUser;
import com.thelazydaniel.taskflow.common.dto.request.PageRequest;
import com.thelazydaniel.taskflow.common.dto.response.PageResponse;
import com.thelazydaniel.taskflow.common.config.TestSecurityConfig;
import com.thelazydaniel.taskflow.user.dto.request.UpdateUserRequest;
import com.thelazydaniel.taskflow.user.dto.request.UpdateUserRoleRequest;
import com.thelazydaniel.taskflow.user.dto.response.UserAdminResponse;
import com.thelazydaniel.taskflow.user.dto.response.UserResponse;
import com.thelazydaniel.taskflow.user.entity.User;
import com.thelazydaniel.taskflow.user.enums.UserRole;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private UserResponse userResponse;
    private UserAdminResponse userAdminResponse;
    private PageResponse<UserAdminResponse> pageResponse;

    @BeforeEach
    void setUp() {
        userResponse = new UserResponse(
                1L,
                "testuser",
                "test@email.com",
                "Test",
                "User",
                UserRole.USER,
                LocalDateTime.now()
        );

        userAdminResponse = new UserAdminResponse(
                1L,
                "testuser",
                "test@email.com",
                "Test",
                "User",
                UserRole.USER,
                true,
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        List<UserAdminResponse> content = List.of(userAdminResponse);
        pageResponse = new PageResponse<>(
                content,
                0,
                10,
                1,
                1,
                true,
                true,
                false
        );
    }

    // ============================================
    // ✅ Custom annotation for test authentication
    // ============================================

    @Retention(RetentionPolicy.RUNTIME)
    @WithSecurityContext(factory = WithSecurityUserSecurityContextFactory.class)
    public @interface WithSecurityUser {
        String username() default "testuser";
        UserRole role() default UserRole.USER;
        long userId() default 1L;
    }

    public static class WithSecurityUserSecurityContextFactory
            implements WithSecurityContextFactory<WithSecurityUser> {

        @Override
        @NonNull
        public SecurityContext createSecurityContext(WithSecurityUser annotation) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();

            // Create User entity
            User user = new User();
            user.setId(annotation.userId());
            user.setUsername(annotation.username());
            user.setEmail(annotation.username() + "@email.com");
            user.setRole(annotation.role());
            user.setEnabled(true);
            user.setAccountNonLocked(true);

            // Create SecurityUser (wraps User entity)
            SecurityUser securityUser = new SecurityUser(user);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            securityUser,  // ✅ Principal is SecurityUser
                            null,
                            securityUser.getAuthorities()
                    );

            context.setAuthentication(auth);
            return context;
        }
    }

    // ============================================
    // ✅ TEST: GET /users/me - Get Current User
    // ============================================

    @Test
    @WithSecurityUser(username = "testuser", role = UserRole.USER)
    void getCurrentUser_ShouldReturnUserResponse_WhenAuthenticated() throws Exception {
        // Arrange
        when(userService.findUserById(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@email.com")))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    void getCurrentUser_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    // ============================================
    // ✅ TEST: PUT /users/me - Update Current User
    // ============================================

    @Test
    @WithSecurityUser(username = "testuser", role = UserRole.USER)
    void updateCurrentUser_ShouldReturnUpdatedUserResponse_WhenValidRequest() throws Exception {
        // Arrange
        when(userService.updateUser(any(UpdateUserRequest.class), anyLong()))
                .thenReturn(userResponse);

        String jsonRequest = """
                {
                    "username": "updateduser",
                    "email": "updated@email.com",
                    "firstName": "Updated",
                    "lastName": "User"
                }
                """;

        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@email.com")));
    }

    @Test
    @WithSecurityUser(username = "testuser", role = UserRole.USER)
    void updateCurrentUser_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        String jsonRequest = """
                {
                    "username": "",
                    "email": "invalid-email",
                    "firstName": "",
                    "lastName": ""
                }
                """;

        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCurrentUser_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        String jsonRequest = """
                {
                    "username": "updateduser",
                    "email": "updated@email.com",
                    "firstName": "Updated",
                    "lastName": "User"
                }
                """;

        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnauthorized());
    }

    // ============================================
    // ✅ TEST: GET /users/{id} - Get User By ID
    // ============================================

    @Test
    @WithSecurityUser(username = "manager", role = UserRole.MANAGER)
    void getUserById_ShouldReturnUserResponse_WhenManager() throws Exception {
        // Arrange
        when(userService.findUserById(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")));
    }

    @Test
    @WithSecurityUser(username = "admin", role = UserRole.ADMIN)
    void getUserById_ShouldReturnUserAdminResponse_WhenAdmin() throws Exception {
        // Arrange
        when(userService.findUserById(1L)).thenReturn(userAdminResponse);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.enabled", is(true)))
                .andExpect(jsonPath("$.accountNonLocked", is(true)));
    }

    @Test
    @WithSecurityUser(username = "testuser", role = UserRole.USER)
    void getUserById_ShouldReturnForbidden_WhenUser() throws Exception {
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserById_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isUnauthorized());
    }

    // ============================================
    // ✅ TEST: GET /users - Get All Users (Admin Only)
    // ============================================

    @Test
    @WithSecurityUser(username = "admin", role = UserRole.ADMIN)
    void getAllUsers_ShouldReturnPageResponse_WhenAdmin() throws Exception {
        // Arrange
        when(userService.getAllUsers(any(PageRequest.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("sortDirection", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].username", is("testuser")))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    @WithSecurityUser(username = "manager", role = UserRole.MANAGER)
    void getAllUsers_ShouldReturnForbidden_WhenManager() throws Exception {
        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithSecurityUser(username = "testuser", role = UserRole.USER)
    void getAllUsers_ShouldReturnForbidden_WhenUser() throws Exception {
        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized());
    }

    // ============================================
    // ✅ TEST: PUT /users/{id}/role - Update User Role (Admin Only)
    // ============================================

    @Test
    @WithSecurityUser(username = "admin", role = UserRole.ADMIN)
    void updateUserRole_ShouldReturnUpdatedUserAdminResponse_WhenAdmin() throws Exception {
        // Arrange
        when(userService.updateUserRole(anyLong(), any(UpdateUserRoleRequest.class)))
                .thenReturn(userAdminResponse);

        String jsonRequest = """
                {
                    "role": "ADMIN"
                }
                """;

        mockMvc.perform(put("/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.enabled", is(true)))
                .andExpect(jsonPath("$.accountNonLocked", is(true)));
    }

    @Test
    @WithSecurityUser(username = "admin", role = UserRole.ADMIN)
    void updateUserRole_ShouldReturnBadRequest_WhenInvalidRole() throws Exception {
        String jsonRequest = """
                {
                    "role": "INVALID_ROLE"
                }
                """;

        mockMvc.perform(put("/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithSecurityUser(username = "manager", role = UserRole.MANAGER)
    void updateUserRole_ShouldReturnForbidden_WhenManager() throws Exception {
        String jsonRequest = """
                {
                    "role": "ADMIN"
                }
                """;

        mockMvc.perform(put("/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUserRole_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        String jsonRequest = """
                {
                    "role": "ADMIN"
                }
                """;

        mockMvc.perform(put("/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnauthorized());
    }

    // ============================================
    // ✅ TEST: DELETE /users/{id} - Delete User (Admin Only)
    // ============================================

    @Test
    @WithSecurityUser(username = "admin", role = UserRole.ADMIN)
    void deleteUserById_ShouldReturnNoContent_WhenAdmin() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUserById(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithSecurityUser(username = "manager", role = UserRole.MANAGER)
    void deleteUserById_ShouldReturnForbidden_WhenManager() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithSecurityUser(username = "testuser", role = UserRole.USER)
    void deleteUserById_ShouldReturnForbidden_WhenUser() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUserById_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isUnauthorized());
    }

    // ============================================
    // ✅ TEST: Error Handling
    // ============================================

    @Test
    @WithSecurityUser(username = "admin", role = UserRole.ADMIN)
    void getUserById_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        // Arrange
        when(userService.findUserById(999L))
                .thenThrow(new com.thelazydaniel.taskflow.user.exception.UserIdNotFoundException(999L));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithSecurityUser(username = "admin", role = UserRole.ADMIN)
    void updateUserRole_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        // Arrange
        when(userService.updateUserRole(anyLong(), any(UpdateUserRoleRequest.class)))
                .thenThrow(new com.thelazydaniel.taskflow.user.exception.UserIdNotFoundException(999L));

        String jsonRequest = """
                {
                    "role": "ADMIN"
                }
                """;

        mockMvc.perform(put("/users/999/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithSecurityUser(username = "admin", role = UserRole.ADMIN)
    void deleteUserById_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        // Arrange
        doThrow(new com.thelazydaniel.taskflow.user.exception.UserIdNotFoundException(999L))
                .when(userService).deleteUserById(999L);

        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isNotFound());
    }
}