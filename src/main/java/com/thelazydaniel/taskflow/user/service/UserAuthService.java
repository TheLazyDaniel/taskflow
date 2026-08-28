package com.thelazydaniel.taskflow.user.service;

import com.thelazydaniel.taskflow.user.UserRepository;
import com.thelazydaniel.taskflow.user.entity.User;
import com.thelazydaniel.taskflow.user.exception.UserNameNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
@Slf4j
public class UserAuthService {
    private final UserRepository userRepository;

    public List<String> getUserRolesFromUsername(String username){
        log.debug("Loading roles for user: username={}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(()-> new UserNameNotFoundException(username));
        return List.of(user.getRole().name());
    }
}
