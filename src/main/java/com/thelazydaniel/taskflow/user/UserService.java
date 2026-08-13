package com.thelazydaniel.taskflow.user;

import com.thelazydaniel.taskflow.common.util.SecurityUtils;
import com.thelazydaniel.taskflow.user.dto.mapper.UserMapper;
import com.thelazydaniel.taskflow.auth.dto.request.RegisterRequest;
import com.thelazydaniel.taskflow.common.dto.request.PageRequest;
import com.thelazydaniel.taskflow.user.dto.request.UpdateUserRequest;
import com.thelazydaniel.taskflow.user.dto.request.UpdateUserRoleRequest;
import com.thelazydaniel.taskflow.common.dto.response.PageResponse;
import com.thelazydaniel.taskflow.user.dto.response.UserAdminResponse;
import com.thelazydaniel.taskflow.user.dto.response.UserResponse;
import com.thelazydaniel.taskflow.user.entity.User;
import com.thelazydaniel.taskflow.user.enums.UserRole;
import com.thelazydaniel.taskflow.user.exception.UserIdNotFoundException;
import com.thelazydaniel.taskflow.common.exception.illegalArgumentException;
import com.thelazydaniel.taskflow.user.exception.unknownUserRoleException;


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
            throw new illegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(registerRequest.email())){
            throw new illegalArgumentException("Email already exists");
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
        if (userRepository.existsByUsername(updateUserRequest.username())){
            throw new illegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(updateUserRequest.email())){
            throw new illegalArgumentException("Email already exists");
        }
        User user = userRepository.findById(id)
                .orElseThrow(()-> new UserIdNotFoundException(id));
        userMapper.updateEntity(updateUserRequest, user);
        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

    @Transactional(readOnly = true)
    public Object findUserById(long id){
        User user =  userRepository.findById(id)
                .orElseThrow(()-> new UserIdNotFoundException(id));
        String currentRole = SecurityUtils.getCurrentUserRole();
        return switch(currentRole){
            case "USER" -> userMapper.toUserResponse(user);
            case "ADMIN" -> userMapper.toUserAdminResponse(user);
            case "MANAGER" -> userMapper.toUserManagerResponse(user);
            default -> throw new unknownUserRoleException(currentRole);
        };
    }
    //Should need rewrite

    @Transactional
    public UserAdminResponse updateUserRole(
            long id,
            UpdateUserRoleRequest updateUserRoleRequest){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserIdNotFoundException(id));
        user.setRole(UserRole.valueOf(updateUserRoleRequest.Role()));
        userRepository.save(user);
        return userMapper.toUserAdminResponse(user);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserAdminResponse> getAllUsers(PageRequest request){
        Pageable pageable = request.toPageable();
        Page<User> users = userRepository.findAll(pageable);
        return PageResponse.from(users, userMapper::toUserAdminResponse);
    }

    @Transactional
    public void deleteUserById(long id) {
        User user =  userRepository.findById(id)
                .orElseThrow(() -> new UserIdNotFoundException(id));
        userRepository.delete(user);
    }

}
