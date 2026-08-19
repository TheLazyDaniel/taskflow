package com.thelazydaniel.taskflow.user;

import com.thelazydaniel.taskflow.common.util.SecurityUtils;
import com.thelazydaniel.taskflow.user.dto.mapper.UserMapper;
import com.thelazydaniel.taskflow.auth.dto.request.RegisterRequest;
import com.thelazydaniel.taskflow.common.dto.request.PageRequest;
import com.thelazydaniel.taskflow.user.dto.request.UpdateUserRequest;
import com.thelazydaniel.taskflow.user.dto.request.UpdateUserRoleRequest;
import com.thelazydaniel.taskflow.common.dto.response.PageResponse;
import com.thelazydaniel.taskflow.user.dto.response.UserResponse;
import com.thelazydaniel.taskflow.user.dto.response.UserSummaryResponse;
import com.thelazydaniel.taskflow.user.entity.User;
import com.thelazydaniel.taskflow.user.enums.UserRole;
import com.thelazydaniel.taskflow.user.exception.UserIdNotFoundException;
import com.thelazydaniel.taskflow.common.exception.IllegalArgumentException;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.username())){
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(registerRequest.email())){
            throw new IllegalArgumentException("Email already exists");
        }
        String hashedPassword = passwordEncoder.encode(registerRequest.password());
        User user = userMapper.toEntity(registerRequest);
        user.setPasswordHash(hashedPassword);
        user.setRole(UserRole.USER);
        //default
        User savedUser = userRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }

    @Transactional
    public UserResponse updateUser(UpdateUserRequest updateUserRequest, long id) {
        if (!updateUserRequest.hasAnyField()) {
            throw new IllegalArgumentException("At least one field must be provided for update");
        }
        if (userRepository.existsByUsername(updateUserRequest.username())){
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(updateUserRequest.email())){
            throw new IllegalArgumentException("Email already exists");
        }

        User user = userRepository.findById(id)
                .orElseThrow(()-> new UserIdNotFoundException(id));
        userMapper.updateEntity(updateUserRequest, user);
        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

    @Transactional
    public UserResponse updateSelf(UpdateUserRequest updateUserRequest){
        User currentUser = SecurityUtils.getCurrentUser();
        if (!updateUserRequest.hasAnyField()) {
            throw new IllegalArgumentException("At least one field must be provided for update");
        }

        if (updateUserRequest.username() != null && !updateUserRequest.username().equals(currentUser.getUsername())) {
            if (userRepository.existsByUsername(updateUserRequest.username())) {
                throw new IllegalArgumentException("Username '" + updateUserRequest.username() + "' is already taken");
            }
            currentUser.setUsername(updateUserRequest.username());
        }

        if (updateUserRequest.email() != null && !updateUserRequest.email().equals(currentUser.getEmail())) {
            if (userRepository.existsByEmail(updateUserRequest.email())) {
                throw new IllegalArgumentException("Email '" + updateUserRequest.email() + "' is already in use");
            }
            currentUser.setEmail(updateUserRequest.email());
        }
        userMapper.updateEntity(updateUserRequest,currentUser);
        userRepository.save(currentUser);
        return userMapper.toUserResponse(currentUser);
    }

    @Transactional(readOnly = true)
    public UserResponse findUserById(long id){
        User user =  userRepository.findById(id)
                .orElseThrow(()-> new UserIdNotFoundException(id));
        return userMapper.toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse findSelf(){
        User currentUser = SecurityUtils.getCurrentUser();
        return userMapper.toUserResponse(currentUser);
    }

    @Transactional
    public UserResponse updateUserRole(
            long id,
            UpdateUserRoleRequest updateUserRoleRequest){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserIdNotFoundException(id));
        user.setRole(UserRole.valueOf(updateUserRoleRequest.role()));
        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserSummaryResponse> getAllUsers(PageRequest request){
        Pageable pageable = request.toPageable();
        Page<User> users = userRepository.findAll(pageable);
        return PageResponse.from(users, userMapper::toUserSummaryResponse);
    }

    @Transactional
    public void deleteUserById(long id) {
        User user =  userRepository.findById(id)
                .orElseThrow(() -> new UserIdNotFoundException(id));
        userRepository.delete(user);
    }

}
