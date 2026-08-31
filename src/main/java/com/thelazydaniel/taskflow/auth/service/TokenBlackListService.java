package com.thelazydaniel.taskflow.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenBlackListService {

    private final RedisTemplate<String,Object> redisTemplate;

    private static final String BLACKLIST_KEY_PREFIX = "token:blacklist:";

    public void blacklistToken(String token, long remainingExpirationMs) {
        if (token == null || token.isEmpty()) {
            log.warn("Attempted to blacklist empty token");
            return;
        }

        if (remainingExpirationMs <= 0) {
            log.debug("Token already expired, skipping blacklist");
            return;
        }

        String key = BLACKLIST_KEY_PREFIX + token;

        try {

            redisTemplate.opsForValue().set(
                    key,
                    "true",
                    Duration.ofMillis(remainingExpirationMs)
            );

            log.debug("Token blacklisted: {} for {}ms",
                    token.substring(0, Math.min(10, token.length())) + "...",
                    remainingExpirationMs);
        } catch (Exception e) {
            log.error("Failed to blacklist token", e);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return true;
        }

        String key = BLACKLIST_KEY_PREFIX + token;

        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Failed to check token blacklist; defaulting to safe (true) block", e);
            return true;
        }
    }

    public void removeFromBlacklist(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }

        String key = BLACKLIST_KEY_PREFIX + token;

        try {
            redisTemplate.delete(key);
            log.debug("Token removed from blacklist");
        } catch (Exception e) {
            log.error("Failed to remove token from blacklist", e);
        }
    }
}
