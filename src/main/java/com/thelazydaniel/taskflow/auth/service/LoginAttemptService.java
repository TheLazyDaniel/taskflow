package com.thelazydaniel.taskflow.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginAttemptService {

    private final RedisTemplate<String,Object> redisTemplate;

    @Value("${security.login.key.user-attempt}")
    private String userAttemptKey;

    @Value("${security.login.key.ip-attempt}")
    private String ipAttemptKey;

    @Value("${security.login.key.user-lock}")
    private String userLockKey;

    @Value("${security.login.key.ip-block}")
    private String ipBlockKey;

    // Configuration constants
    @Value("${security.login.max-attempts}")
    private int maxAttempts;

    @Value("${security.login.account-lock-minutes}")
    private int accountLockMinutes;

    @Value("${security.login.ip-block-minutes}")
    private int ipBlockMinutes;

    @Value("${security.login.attempt-window-minutes}")
    private int attemptWindowMinutes;

    public void loginFailed(String username, String ipAddress) {
        // Record user failure
        recordUserFailure(username);

        // Record IP failure
        recordIpFailure(ipAddress);
    }

    private void recordUserFailure(String username) {
        String attemptKey = userAttemptKey + username;
        String lockKey = userLockKey + username;

        try {
            // Increment attempt counter
            Long attempts = redisTemplate.opsForValue().increment(attemptKey);

            // Set expiry for attempt counter (whole sequence of trials a window)
            if (attempts != null && attempts == 1 ){
                redisTemplate.expire(attemptKey, Duration.ofMinutes(attemptWindowMinutes));
            }

            if (attempts != null && attempts >= maxAttempts) {
                // Lock account
                redisTemplate.opsForValue().set(lockKey, "locked",
                        Duration.ofMinutes(accountLockMinutes));

                // Reset attempt counter
                redisTemplate.delete(attemptKey);

                log.warn("Account locked: {} after {} failed attempts",
                        username, maxAttempts);
            } else {
                int remaining = maxAttempts - (attempts != null ? attempts.intValue() : 0);
                log.debug("Failed login for user: {}, attempts: {}, remaining: {}",
                        username, attempts, remaining);
            }
        } catch (Exception e) {
            log.error("Failed to record user login failure", e);
        }
    }

    private void recordIpFailure(String ipAddress) {
        String attemptKey = ipAttemptKey + ipAddress;
        String blockKey = ipBlockKey + ipAddress;

        try {
            // Increment IP attempt counter
            Long attempts = redisTemplate.opsForValue().increment(attemptKey);

            // Set expiry for attempt counter (whole sequence of trials a window)
            if (attempts != null && attempts == 1) {
                redisTemplate.expire(attemptKey, Duration.ofMinutes(attemptWindowMinutes));
            }

            if (attempts != null && attempts >= maxAttempts) {
                // Block IP
                redisTemplate.opsForValue().set(blockKey, "blocked",
                        Duration.ofMinutes(ipBlockMinutes));

                // Reset attempt counter
                redisTemplate.delete(attemptKey);

                log.warn("IP blocked: {} after {} failed attempts",
                        ipAddress, maxAttempts);
            }
        } catch (Exception e) {
            log.error("Failed to record IP login failure", e);
        }
    }

    public void loginSucceeded(String username, String ipAddress) {
        redisTemplate.delete(userAttemptKey + username);
        redisTemplate.delete(userLockKey + username);
        redisTemplate.delete(ipAttemptKey + ipAddress);
        redisTemplate.delete(ipBlockKey + ipAddress);

        log.debug("Login succeeded for user: {} from IP: {}", username, ipAddress);
    }

    public boolean isAccountLocked(String username) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(userLockKey + username));
    }

    public boolean isIpBlocked(String ipAddress) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(ipBlockKey + ipAddress));
    }

    public int getRemainingAttempts(String username, String ipAddress) {
        String userKey = userAttemptKey + username;
        Object attempts = redisTemplate.opsForValue().get(userKey);

        if (attempts == null) {
            return maxAttempts;
        }
        try {
            return Math.max(0, maxAttempts - Integer.parseInt(attempts.toString()));
        } catch (NumberFormatException e) {
            return maxAttempts;
        }
    }

    public long getAccountLockRemainingTime(String username) {
        String lockKey = userLockKey + username;
        Long ttl = redisTemplate.getExpire(lockKey, TimeUnit.MINUTES);
        return (ttl != null && ttl > 0) ? ttl : 0;
    }

    public long getIpBlockRemainingTime(String ipAddress) {
        String blockKey = ipBlockKey + ipAddress;
        Long ttl = redisTemplate.getExpire(blockKey, TimeUnit.MINUTES);
        return (ttl != null && ttl > 0) ? ttl : 0;
    }

    public void unlockAccount(String username) {
        redisTemplate.delete(userLockKey + username);
        redisTemplate.delete(userAttemptKey + username);
        log.info("Account manually unlocked: {}", username);
    }

    public void unblockIp(String ipAddress) {
        redisTemplate.delete(ipBlockKey + ipAddress);
        redisTemplate.delete(ipAttemptKey + ipAddress);
        log.info("IP manually unblocked: {}", ipAddress);
    }

}
