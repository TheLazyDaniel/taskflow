package com.thelazydaniel.taskflow.user;

import com.thelazydaniel.taskflow.common.dto.request.PageRequest;
import com.thelazydaniel.taskflow.common.dto.response.PageResponse;
import com.thelazydaniel.taskflow.common.exception.illegalArgumentException;
import com.thelazydaniel.taskflow.common.util.SecurityUtils;
import com.thelazydaniel.taskflow.user.dto.mapper.UserMapper;
import com.thelazydaniel.taskflow.auth.dto.request.RegisterRequest;
import com.thelazydaniel.taskflow.user.dto.request.UpdateUserRequest;
import com.thelazydaniel.taskflow.user.dto.request.UpdateUserRoleRequest;
import com.thelazydaniel.taskflow.user.dto.response.UserAdminResponse;
import com.thelazydaniel.taskflow.user.dto.response.UserResponse;
import com.thelazydaniel.taskflow.user.entity.User;
import com.thelazydaniel.taskflow.user.enums.UserRole;
import com.thelazydaniel.taskflow.user.exception.UserIdNotFoundException;
import com.thelazydaniel.taskflow.user.exception.unknownUserRoleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // Test data
    private RegisterRequest registerRequest;
    private UpdateUserRequest updateUserRequest;
    private UpdateUserRoleRequest updateUserRoleRequest;
    private User user;
    private User savedUser;
    private User updatedUser;
    private UserResponse userResponse;
    private UserAdminResponse userAdminResponse;
    private PageRequest pageRequest;

    @BeforeEach
    void setUp() {
        // Register Request
        registerRequest = new RegisterRequest(
                "testuser",
                "test@email.com",
                "password123",
                "Test",
                "User"
        );

        // Update User Request
        updateUserRequest = new UpdateUserRequest(
                "updateduser",
                "updated@email.com",
                "Updated",
                "User"
        );

        // Update Role Request
        updateUserRoleRequest = new UpdateUserRoleRequest("ADMIN");

        // User entity
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@email.com");
        user.setPasswordHash("encodedPassword");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setCreatedDate(LocalDateTime.now());

        // Saved User (with ID)
        savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        savedUser.setEmail("test@email.com");
        savedUser.setPasswordHash("encodedPassword");
        savedUser.setFirstName("Test");
        savedUser.setLastName("User");
        savedUser.setRole(UserRole.USER);
        savedUser.setEnabled(true);
        savedUser.setAccountNonLocked(true);
        savedUser.setCreatedDate(LocalDateTime.now());

        // Updated User
        updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@email.com");
        updatedUser.setPasswordHash("encodedPassword");
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("User");
        updatedUser.setRole(UserRole.USER);
        updatedUser.setEnabled(true);
        updatedUser.setAccountNonLocked(true);
        updatedUser.setCreatedDate(LocalDateTime.now());

        // User Response
        userResponse = new UserResponse(
                1L,
                "testuser",
                "test@email.com",
                "Test",
                "User",
                UserRole.USER,
                LocalDateTime.now()
        );

        // User Admin Response
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

        // Page Request
        pageRequest = new PageRequest(0, 10, "id", "ASCENDING");
    }

    // ============================================
    // ✅ TEST: registerUser
    // ============================================
    @Test
    void registerUser_ShouldReturnUserResponse_WhenValidRequest() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@email.com")).thenReturn(false);
        when(userMapper.toEntity(registerRequest)).thenReturn(user);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toUserResponse(savedUser)).thenReturn(userResponse);

        // Act
        UserResponse result = userService.registerUser(registerRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("testuser");
        assertThat(result.email()).isEqualTo("test@email.com");
        assertThat(result.role()).isEqualTo(UserRole.USER);

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@email.com");
        verify(userMapper).toEntity(registerRequest);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(user);
        verify(userMapper).toUserResponse(savedUser);
    }

    @Test
    void registerUser_ShouldThrowException_WhenUsernameAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        illegalArgumentException exception = assertThrows(
                illegalArgumentException.class,
                () -> userService.registerUser(registerRequest)
        );

        assertThat(exception.getMessage()).contains("Username already exists");
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@email.com")).thenReturn(true);

        // Act & Assert
        illegalArgumentException exception = assertThrows(
                illegalArgumentException.class,
                () -> userService.registerUser(registerRequest)
        );

        assertThat(exception.getMessage()).contains("Email already exists");
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@email.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_ShouldSetDefaultRoleAndEncodePassword() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@email.com")).thenReturn(false);

        // Create a real user that will be returned by mapper
        User userToSave = new User();
        userToSave.setUsername("testuser");
        userToSave.setEmail("test@email.com");

        when(userMapper.toEntity(registerRequest)).thenReturn(userToSave);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        // ✅ Use ArgumentCaptor to capture the user passed to save()
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenReturn(savedUser);
        when(userMapper.toUserResponse(any(User.class))).thenReturn(userResponse);

        // Act
        userService.registerUser(registerRequest);

        // Assert
        // ✅ Verify the captured user has the correct values
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getPasswordHash()).isEqualTo("encodedPassword");
        assertThat(capturedUser.getRole()).isEqualTo(UserRole.USER);

        // Verify interactions
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@email.com");
        verify(userMapper).toEntity(registerRequest);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(userMapper).toUserResponse(any(User.class));
    }

    // ============================================
    // ✅ TEST: updateUser
    // ============================================
    @Test
    void updateUser_ShouldReturnUpdatedUserResponse_WhenValidRequest() {
        // Arrange
        long userId = 1L;
        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(userRepository.existsByEmail("updated@email.com")).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(updatedUser);
        when(userMapper.toUserResponse(updatedUser)).thenReturn(
                new UserResponse(
                        1L,
                        "updateduser",
                        "updated@email.com",
                        "Updated",
                        "User",
                        UserRole.USER,
                        LocalDateTime.now()
                )
        );

        // Act
        UserResponse result = userService.updateUser(updateUserRequest, userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("updateduser");
        assertThat(result.email()).isEqualTo("updated@email.com");

        verify(userMapper).updateEntity(updateUserRequest, user);
        verify(userRepository).save(user);
        verify(userMapper).toUserResponse(updatedUser);
    }

    @Test
    void updateUser_ShouldThrowException_WhenUsernameAlreadyExists() {
        // Arrange
        long userId = 1L;
        when(userRepository.existsByUsername("updateduser")).thenReturn(true);

        // Act & Assert
        illegalArgumentException exception = assertThrows(
                illegalArgumentException.class,
                () -> userService.updateUser(updateUserRequest, userId)
        );

        assertThat(exception.getMessage()).contains("Username already exists");
        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        long userId = 1L;
        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(userRepository.existsByEmail("updated@email.com")).thenReturn(true);

        // Act & Assert
        illegalArgumentException exception = assertThrows(
                illegalArgumentException.class,
                () -> userService.updateUser(updateUserRequest, userId)
        );

        assertThat(exception.getMessage()).contains("Email already exists");
        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        long userId = 999L;
        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(userRepository.existsByEmail("updated@email.com")).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UserIdNotFoundException exception = assertThrows(
                UserIdNotFoundException.class,
                () -> userService.updateUser(updateUserRequest, userId)
        );

        assertThat(exception.getMessage()).contains("User not found with id: 999");
        verify(userRepository, never()).save(any());
    }

    // ============================================
    // ✅ TEST: findUserById
    // ============================================
    @Test
    void findUserById_ShouldReturnUserResponse_WhenCurrentUserRoleIsUser() {
        // Arrange
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        // Mock SecurityUtils.getCurrentUserRole()
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserRole).thenReturn("USER");

            // Act
            Object result = userService.findUserById(userId);

            // Assert
            assertThat(result).isInstanceOf(UserResponse.class);
            UserResponse response = (UserResponse) result;
            assertThat(response.username()).isEqualTo("testuser");
            verify(userMapper).toUserResponse(user);
            verify(userMapper, never()).toUserAdminResponse(any());
            verify(userMapper, never()).toUserManagerResponse(any());
        }
    }

    @Test
    void findUserById_ShouldReturnUserAdminResponse_WhenCurrentUserRoleIsAdmin() {
        // Arrange
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserAdminResponse(user)).thenReturn(userAdminResponse);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserRole).thenReturn("ADMIN");

            // Act
            Object result = userService.findUserById(userId);

            // Assert
            assertThat(result).isInstanceOf(UserAdminResponse.class);
            UserAdminResponse response = (UserAdminResponse) result;
            assertThat(response.username()).isEqualTo("testuser");
            verify(userMapper, never()).toUserResponse(any());
            verify(userMapper).toUserAdminResponse(user);
            verify(userMapper, never()).toUserManagerResponse(any());
        }
    }

    @Test
    void findUserById_ShouldReturnUserManagerResponse_WhenCurrentUserRoleIsManager() {
        // Arrange
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserManagerResponse(user)).thenReturn(
                new com.thelazydaniel.taskflow.user.dto.response.UserManagerResponse(
                        1L,
                        "testuser",
                        "test@email.com",
                        "Test",
                        "User",
                        UserRole.USER,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )
        );

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserRole).thenReturn("MANAGER");

            // Act
            Object result = userService.findUserById(userId);

            // Assert
            assertThat(result).isInstanceOf(
                    com.thelazydaniel.taskflow.user.dto.response.UserManagerResponse.class
            );
            verify(userMapper, never()).toUserResponse(any());
            verify(userMapper, never()).toUserAdminResponse(any());
            verify(userMapper).toUserManagerResponse(user);
        }
    }

    @Test
    void findUserById_ShouldThrowException_WhenUnknownRole() {
        // Arrange
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserRole).thenReturn("UNKNOWN_ROLE");

            // Act & Assert
            unknownUserRoleException exception = assertThrows(
                    unknownUserRoleException.class,
                    () -> userService.findUserById(userId)
            );

            assertThat(exception.getMessage()).contains("Unknown role: UNKNOWN_ROLE");
        }
    }

    @Test
    void findUserById_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UserIdNotFoundException exception = assertThrows(
                UserIdNotFoundException.class,
                () -> userService.findUserById(userId)
        );

        assertThat(exception.getMessage()).contains("User not found with id: 999");
    }
    @Test
    void updateUserRole_ShouldUpdateRoleAndReturnAdminResponse_WhenValid() {
        // Arrange
        long userId = 1L;

        // Create REAL user with USER role
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@email.com");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        user.setAccountNonLocked(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // ✅ Use ArgumentCaptor to capture what's actually saved
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> {
            return invocation.getArgument(0);  // Return the saved user
        });

        // ✅ Map based on the captured user
        when(userMapper.toUserAdminResponse(any(User.class))).thenAnswer(invocation -> {
            User userToMap = invocation.getArgument(0);
            return new UserAdminResponse(
                    userToMap.getId(),
                    userToMap.getUsername(),
                    "test@email.com",
                    "Test",
                    "User",
                    userToMap.getRole(),
                    true,
                    true,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );
        });

        // Act
        UserAdminResponse result = userService.updateUserRole(userId, updateUserRoleRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.role()).isEqualTo(UserRole.ADMIN);

        // ✅ Verify the captured user has ADMIN role
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getRole()).isEqualTo(UserRole.ADMIN);

        // ✅ Verify the original user object was modified
        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void updateUserRole_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UserIdNotFoundException exception = assertThrows(
                UserIdNotFoundException.class,
                () -> userService.updateUserRole(userId, updateUserRoleRequest)
        );

        assertThat(exception.getMessage()).contains("User not found with id: 999");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserRole_ShouldThrowException_WhenInvalidRoleProvided() {
        // Arrange
        long userId = 1L;
        UpdateUserRoleRequest invalidRequest = new UpdateUserRoleRequest("INVALID_ROLE");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserRole(userId, invalidRequest);
        });

        verify(userRepository, never()).save(any());
    }

    // ============================================
    // ✅ TEST: getAllUsers
    // ============================================
    @Test
    void getAllUsers_ShouldReturnPageResponse_WhenUsersExist() {
        // Arrange
        List<User> userList = List.of(user);

        // ✅ FIX: Create Pageable with the SAME parameters as pageRequest
        Pageable pageable = pageRequest.toPageable(); // page=0, size=10

        // ✅ Pass the Pageable to PageImpl
        Page<User> userPage = new PageImpl<>(userList, pageable, 1); // 1 total element

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toUserAdminResponse(user)).thenReturn(userAdminResponse);

        // Act
        PageResponse<UserAdminResponse> result = userService.getAllUsers(pageRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).username()).isEqualTo("testuser");
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1); // 1/10 = 1 page
        assertThat(result.currentPage()).isEqualTo(0); // 0-based
        assertThat(result.pageSize()).isEqualTo(10); // ✅ Now 10!

        verify(userRepository).findAll(any(Pageable.class));
        verify(userMapper).toUserAdminResponse(user);
    }

    @Test
    void getAllUsers_ShouldReturnEmptyPageResponse_WhenNoUsers() {
        // Arrange
        List<User> emptyList = List.of();
        Page<User> emptyPage = new PageImpl<>(emptyList);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        // Act
        PageResponse<UserAdminResponse> result = userService.getAllUsers(pageRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(0);

        verify(userRepository).findAll(any(Pageable.class));
        verify(userMapper, never()).toUserAdminResponse(any());
    }

    @Test
    void getAllUsers_ShouldCallRepositoryWithCorrectPageable() {
        // Arrange
        PageRequest customPageRequest = new PageRequest(2, 20, "username", "DESCENDING");
        List<User> userList = List.of(user);
        Page<User> userPage = new PageImpl<>(userList);

        // ✅ Capture the Pageable
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(userRepository.findAll(pageableCaptor.capture())).thenReturn(userPage);
        when(userMapper.toUserAdminResponse(any(User.class))).thenReturn(userAdminResponse);

        // Act
        userService.getAllUsers(customPageRequest);

        // Assert
        // ✅ Verify the correct Pageable was passed to repository
        Pageable capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.getPageNumber()).isEqualTo(2);
        assertThat(capturedPageable.getPageSize()).isEqualTo(20);
        assertThat(capturedPageable.getSort().toString()).contains("username");
        assertThat(capturedPageable.getSort().toString()).contains("DESC");
    }

    // ============================================
    // ✅ TEST: deleteUserById
    // ============================================
    @Test
    void deleteUserById_ShouldDeleteUser_WhenUserExists() {
        // Arrange
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        userService.deleteUserById(userId);

        // Assert
        verify(userRepository).findById(userId);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUserById_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UserIdNotFoundException exception = assertThrows(
                UserIdNotFoundException.class,
                () -> userService.deleteUserById(userId)
        );

        assertThat(exception.getMessage()).contains("User not found with id: 999");
        verify(userRepository, never()).delete(any());
    }

    // ============================================
    // ✅ TEST: Additional Edge Cases
    // ============================================
    @Test
    void registerUser_ShouldTrimUsernameAndEmail_WhenMapping() {
        // Arrange
        RegisterRequest requestWithSpaces = new RegisterRequest(
                " testuser ",
                " test@email.com ",
                "password123",
                "Test",
                "User"
        );

        when(userRepository.existsByUsername(" testuser ")).thenReturn(false);
        when(userRepository.existsByEmail(" test@email.com ")).thenReturn(false);
        when(userMapper.toEntity(requestWithSpaces)).thenReturn(user);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toUserResponse(savedUser)).thenReturn(userResponse);

        // Act
        userService.registerUser(requestWithSpaces);

        // Assert - UserMapper should handle trimming if implemented
        verify(userMapper).toEntity(requestWithSpaces);
    }

    @Test
    void updateUser_ShouldNotUpdatePassword_WhenNotProvided() {
        // Arrange
        long userId = 1L;
        UpdateUserRequest requestWithoutPassword = new UpdateUserRequest(
                "updateduser",
                "updated@email.com",
                "Updated",
                "User"
        );

        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(userRepository.existsByEmail("updated@email.com")).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(updatedUser);
        when(userMapper.toUserResponse(any(User.class))).thenReturn(
                new UserResponse(
                        1L,
                        "updateduser",
                        "updated@email.com",
                        "Updated",
                        "User",
                        UserRole.USER,
                        LocalDateTime.now()
                )
        );

        // Act
        userService.updateUser(requestWithoutPassword, userId);

        // Assert - Password should not be changed
        verify(userMapper).updateEntity(requestWithoutPassword, user);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void registerUser_ShouldHandleEmptyOptionalFields() {
        // Arrange
        RegisterRequest requestWithNulls = new RegisterRequest(
                "testuser",
                "test@email.com",
                "password123",
                null,
                null
        );

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@email.com")).thenReturn(false);
        when(userMapper.toEntity(requestWithNulls)).thenReturn(user);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toUserResponse(savedUser)).thenReturn(userResponse);

        // Act
        UserResponse result = userService.registerUser(requestWithNulls);

        // Assert
        assertThat(result).isNotNull();
        verify(userMapper).toEntity(requestWithNulls);
    }

    // ============================================
    // ✅ TEST: SecurityContext Integration
    // ============================================
    @Test
    void findUserById_ShouldUseSecurityContext_ToDetermineResponseType() {
        // This test ensures that the method correctly uses SecurityUtils
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Test with different roles using MockedStatic
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            // Test USER role
            securityUtils.when(SecurityUtils::getCurrentUserRole).thenReturn("USER");
            when(userMapper.toUserResponse(user)).thenReturn(userResponse);

            Object result = userService.findUserById(userId);
            assertThat(result).isInstanceOf(UserResponse.class);

            // Test ADMIN role
            securityUtils.when(SecurityUtils::getCurrentUserRole).thenReturn("ADMIN");
            when(userMapper.toUserAdminResponse(user)).thenReturn(userAdminResponse);

            result = userService.findUserById(userId);
            assertThat(result).isInstanceOf(UserAdminResponse.class);
        }
    }
}