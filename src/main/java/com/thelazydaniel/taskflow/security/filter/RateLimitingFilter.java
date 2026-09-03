package com.thelazydaniel.taskflow.security.filter;


import com.thelazydaniel.taskflow.common.dto.response.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    @Value("${security.rate-limit.key-prefix}")
    private String rateLimitPrefix;

    @Value("${security.rate-limit.max-requests-per-minute}")
    private int maxRequestsPerMinute;

    @Value("${security.rate-limit.request-window-minutes}")
    private int attemptWindowMinutes;

    private final RedisTemplate<String,Object> redisTemplate;


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String ipAddress = getClientIP(request);
        String endpoint = request.getRequestURI();

        if (isStaticResource(endpoint)) {
            filterChain.doFilter(request, response);
            return;
        }
        String rateLimitKey = rateLimitPrefix + ipAddress + ":" + normalizeEndpoint(endpoint);


        try {
            Long requestCount = redisTemplate.opsForValue().increment(rateLimitKey);

            if (requestCount != null && requestCount == 1) {
                redisTemplate.expire(rateLimitKey, Duration.ofMinutes(attemptWindowMinutes));
            }

            if (requestCount != null && requestCount > maxRequestsPerMinute){
                sendErrorResponse(request,response);
                return;
            }

            response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequestsPerMinute));
            response.setHeader("X-RateLimit-Remaining",
                    String.valueOf(Math.max(0, maxRequestsPerMinute - (requestCount == null? 0 : requestCount))));
            filterChain.doFilter(request,response);
        } catch (Exception e) {
        log.error("Error in rate limiting", e);
        // Fail-open: allow request if Redis is down
        filterChain.doFilter(request, response);
        }
    }

    private void sendErrorResponse(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        log.warn("Sending error response for request: {} {}", request.getMethod(), request.getRequestURI());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .error(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase())
                .message("Too many requests. Please try again later")
                .path(request.getRequestURI())
                .build();

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
    }

    private boolean isStaticResource(String path) {
        return path.startsWith("/static/") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.startsWith("/fonts/") ||
                path.startsWith("/favicon.ico") ||
                path.startsWith("/robots.txt") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs");
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    private String normalizeEndpoint(String endpoint) {
        // Remove path variables
        String normalized = endpoint.replaceAll("/\\d+", "/{id}");

        // Group by resource type
        if (normalized.startsWith("/api/auth")) {
            return "/api/auth";
        }
        if (normalized.startsWith("/api/users")) {
            return "/api/users";
        }
        if (normalized.startsWith("/api/projects")) {
            return "/api/projects";
        }

        return normalized;
    }
}
