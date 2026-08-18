package com.thelazydaniel.taskflow.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thelazydaniel.taskflow.common.util.SecurityUtils;
import com.thelazydaniel.taskflow.common.dto.request.PageRequest;
import com.thelazydaniel.taskflow.common.dto.response.PageResponse;
import com.thelazydaniel.taskflow.user.dto.request.UpdateUserRequest;
import com.thelazydaniel.taskflow.user.dto.request.UpdateUserRoleRequest;
import com.thelazydaniel.taskflow.user.dto.response.UserAdminResponse;
import com.thelazydaniel.taskflow.user.dto.response.UserResponse;
import com.thelazydaniel.taskflow.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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

        pageResponse = new PageResponse<>(List.of(userAdminResponse), 0, 10, 1, 1, true, true, false);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_allowedForAdmin() throws Exception {
        PageRequest pr = new PageRequest(0, 10, "id", "ASCENDING");
        when(userService.getAllUsers(any(PageRequest.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("sortDir", "ASCENDING")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(pageResponse)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_forbiddenForUser() throws Exception {
        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("sortDir", "ASCENDING")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getUserById_allowedForManager() throws Exception {
        when(userService.findUserById(2L)).thenReturn(userAdminResponse);

        mockMvc.perform(get("/users/2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(userAdminResponse)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserById_forbiddenForUser() throws Exception {
        mockMvc.perform(get("/users/2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_allowedForAdmin() throws Exception {
        UpdateUserRoleRequest req = new UpdateUserRoleRequest("ADMIN");
        when(userService.updateUserRole(anyLong(), any(UpdateUserRoleRequest.class))).thenReturn(userAdminResponse);

        mockMvc.perform(put("/users/3/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(userAdminResponse)));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void updateUserRole_forbiddenForManager() throws Exception {
        UpdateUserRoleRequest req = new UpdateUserRoleRequest("ADMIN");

        mockMvc.perform(put("/users/3/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_allowedForAdmin() throws Exception {
        mockMvc.perform(delete("/users/4"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteUser_forbiddenForUser() throws Exception {
        mockMvc.perform(delete("/users/4"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCurrentUser_allowedForAuthenticated() throws Exception {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            when(userService.findUserById(1L)).thenReturn(userResponse);

            mockMvc.perform(get("/users/me").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(userResponse)));
        }
    }

    @Test
    void getCurrentUser_unauthorizedWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/users/me").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateCurrentUser_allowedForAuthenticated() throws Exception {
        UpdateUserRequest req = new UpdateUserRequest("updateduser", "updated@email.com", "Updated", "User");
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            when(userService.updateUser(any(UpdateUserRequest.class), anyLong())).thenReturn(userResponse);

            mockMvc.perform(put("/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(userResponse)));
        }
    }

    @Test
    void updateCurrentUser_unauthorizedWhenNotAuthenticated() throws Exception {
        UpdateUserRequest req = new UpdateUserRequest("updateduser", "updated@email.com", "Updated", "User");
        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
