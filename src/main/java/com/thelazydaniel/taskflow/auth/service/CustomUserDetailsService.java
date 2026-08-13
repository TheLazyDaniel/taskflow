package com.thelazydaniel.taskflow.auth.service;

import com.thelazydaniel.taskflow.auth.entity.SecurityUser;
import com.thelazydaniel.taskflow.auth.exception.AccountDisabledException;
import com.thelazydaniel.taskflow.auth.exception.AccountLockedException;
import com.thelazydaniel.taskflow.user.UserRepository;
import com.thelazydaniel.taskflow.user.entity.User;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);

        long startTime = System.currentTimeMillis();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found by username: {}", username);
                    return new UsernameNotFoundException(username);
                });

        if (!user.isEnabled()) {
            log.warn("User is disabled by username: {}", username);
            throw new AccountDisabledException(username);
        }

        if (!user.isAccountNonLocked()){
            log.warn("User is locked by username: {}", username);
            throw new AccountLockedException(username);
        }
        long duration = System.currentTimeMillis() - startTime;
        log.info("User loaded in: {} ms", duration);
        return new SecurityUser(user);
    }
}
