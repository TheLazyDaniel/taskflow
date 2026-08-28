package com.thelazydaniel.taskflow.user.service;


import com.thelazydaniel.taskflow.common.exception.IllegalArgumentException;
import com.thelazydaniel.taskflow.common.util.SecurityUtils;
import com.thelazydaniel.taskflow.user.UserRepository;
import com.thelazydaniel.taskflow.user.entity.User;
import com.thelazydaniel.taskflow.user.exception.UserIdNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@Transactional(readOnly = true)
@Slf4j
public class UserValidationService {
    private final UserRepository userRepository;

    public UserValidationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Long getCurrentUserId(){
        return SecurityUtils.getCurrentUserId();
    }

    public User getCurrentUser(){
        return SecurityUtils.getCurrentUser();
    }

    public void validateUser(Long id){
        userRepository.findById(id)
                .orElseThrow(()-> new UserIdNotFoundException(id));
    }

    public User validateAndGetUser(Long id){
        log.debug("Validating user: userId={}", id);
        return userRepository.findById(id)
                .orElseThrow(()-> new UserIdNotFoundException(id));
    }

    public void validateUsernameExistence(String username){
        log.debug("Validating username availability: username={}", username);
        if (userRepository.existsByUsername(username)){
            log.warn("Username already exists: username={}", username);
            throw new IllegalArgumentException(String.format("User with username: %s already exists",username));
        }
    }

    public void validateEmailExistence(String email){
        log.debug("Validating email availability");
        if (userRepository.existsByEmail(email)){
            log.warn("Email already exists");
            throw new IllegalArgumentException(String.format("User with email: %s already exists",email));
        }
    }
}
