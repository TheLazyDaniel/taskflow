package com.thelazydaniel.taskflow.user.service;


import com.thelazydaniel.taskflow.common.exception.IllegalArgumentException;
import com.thelazydaniel.taskflow.user.UserRepository;
import com.thelazydaniel.taskflow.user.entity.User;
import com.thelazydaniel.taskflow.user.enums.UserRole;
import com.thelazydaniel.taskflow.user.exception.UserIdNotFoundException;
import com.thelazydaniel.taskflow.user.exception.UserNameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserValidationService {
    private final UserRepository userRepository;

    public UserValidationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User validateAndGetUser(Long id){
        return userRepository.findById(id)
                .orElseThrow(()-> new UserIdNotFoundException(id));
    }

    //for auth service only
    public List<String> getUserRolesFromUsername(String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(()-> new UserNameNotFoundException(username));
        return List.of(user.getRole().name());
    }

    public void validateUsernameExistence(String username){
        if (userRepository.existsByUsername(username)){
            throw new IllegalArgumentException(String.format("User with username: %s already exists",username));
        }
    }

    public void validateEmailExistence(String email){
        if (userRepository.existsByEmail(email)){
            throw new IllegalArgumentException(String.format("User with email: %s already exists",email));
        }
    }
}
