package com.thelazydaniel.taskflow.security.jwt;

import com.thelazydaniel.taskflow.security.TokenType;
import com.thelazydaniel.taskflow.security.TokenValidationResult;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {


    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.refresh-secret}")
    private String jwtRefreshSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long jwtRefreshExpirationMs;

    private static final Map<TokenType, String> TOKEN_TYPE_CLAIMS = Map.of(
            TokenType.ACCESS, "access",
            TokenType.REFRESH, "refresh"
    );


    public String generateJwtToken(Authentication authentication) {

        if (authentication == null) {
            throw new IllegalArgumentException("Authentication cannot be null");
        }

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            throw new IllegalArgumentException("Authentication principal cannot be null");
        }

        if (!(principal instanceof UserDetails userDetails)) {
            throw new IllegalArgumentException(
                    "Expected UserDetails but got: " + principal.getClass().getName());
        }

        String username = userDetails.getUsername();
        if (username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        return generateTokenFromUsername(userDetails.getUsername(),
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));

    }

    public String generateTokenFromUsername(
            String username,
            List<String> authorities) {
        log.debug("Generating JWT Token for username: {}, roles: {}", username, authorities);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", authorities);
        claims.put("type", "access");

        log.debug("Generated JWT Token for username: {}, roles: {}", username, claims);

        return buildToken(claims, username, jwtExpirationMs, getAccessTokenSigningKey());

    }

    public String generateRefreshToken(String username, List<String> authorities) {
        log.debug("Generating refresh token for username: {}", username);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", authorities);
        claims.put("type", "refresh");

        return buildToken(claims, username, jwtRefreshExpirationMs, getRefreshTokenSigningKey());
    }

    public Claims getAllClaimsFromToken(String token, SecretKey signingKey){
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver,
            SecretKey signingKey) {
        final Claims claims = getAllClaimsFromToken(token, signingKey);
        return claimsResolver.apply(claims);
    }

    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject, getAccessTokenSigningKey());
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token, getAccessTokenSigningKey());
        List<String> roles = claims.get("roles", List.class);
        return roles != null ? roles : Collections.emptyList();
    }

    public String getUsernameFromRefreshToken(String token) {
        return extractClaim(token, Claims::getSubject, getRefreshTokenSigningKey());
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromRefreshToken(String token) {
        Claims claims = getAllClaimsFromToken(token, getRefreshTokenSigningKey());
        List<String> roles = claims.get("roles", List.class);
        return roles != null ? roles : Collections.emptyList();
    }

    public long getRemainingExpirationMsFromRefreshToken(String refreshToken) {
        try {
            Date expirationDate = getExpirationFromRefreshToken(refreshToken);
            Date now = new Date();

            long remainingMs = expirationDate.getTime() - now.getTime();

            // Return 0 if token is already expired
            return Math.max(0, remainingMs);

        } catch (ExpiredJwtException e) {
            log.debug("Refresh token already expired");
            return 0;
        } catch (Exception e) {
            log.error("Error getting expiration from refresh token: {}", e.getMessage());
            return 0;
        }
    }

    public Date getExpirationFromRefreshToken(String token) {
        Claims claims = getAllClaimsFromToken(token, getRefreshTokenSigningKey());
        return claims.getExpiration();
    }

    public long getRemainingExpirationMsFromAccessToken(String accessToken) {
        try {
            Date expirationDate = getExpirationFromAccessToken(accessToken);
            Date now = new Date();

            long remainingMs = expirationDate.getTime() - now.getTime();

            return Math.max(0, remainingMs);

        } catch (ExpiredJwtException e) {
            log.debug("Access token already expired");
            return 0;
        } catch (Exception e) {
            log.error("Error getting expiration from access token: {}", e.getMessage());
            return 0;
        }
    }

    public Date getExpirationFromAccessToken(String token) {
        Claims claims = getAllClaimsFromToken(token, getAccessTokenSigningKey());
        return claims.getExpiration();
    }

    public TokenValidationResult validateToken(String token, TokenType expectedType) {
        SecretKey signingKey = getSigningKeyForTokenType(expectedType);
        try {
            Claims claims = getAllClaimsFromToken(token,signingKey);

            // Check token type
            String expectedClaim = TOKEN_TYPE_CLAIMS.get(expectedType);
            String tokenType = claims.get("type", String.class);
            if (tokenType == null || !tokenType.equals(expectedClaim)) {
                return TokenValidationResult.invalid("Invalid token type");
            }

            // Check expiration
            if (claims.getExpiration().before(new Date())) {
                return TokenValidationResult.expired("Token has expired");
            }

            return TokenValidationResult.valid(claims);

        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
            return TokenValidationResult.expired("Token has expired");
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            return TokenValidationResult.invalid("Invalid token signature");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            return TokenValidationResult.invalid("Unsupported token");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            return TokenValidationResult.invalid("Invalid token");
        }
    }


    private SecretKey getAccessTokenSigningKey() {
        return getSigningKey(jwtSecret);
    }

    private SecretKey getRefreshTokenSigningKey() {
        return getSigningKey(jwtRefreshSecret);
    }

    private SecretKey getSigningKey(String secret) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey getSigningKeyForTokenType(TokenType tokenType) {
        return switch (tokenType) {
            case ACCESS -> getAccessTokenSigningKey();
            case REFRESH -> getRefreshTokenSigningKey();
        };
    }

    private String buildToken(
            Map<String, Object> claims,
            String subject,
            long expirationMs,
            SecretKey signingKey) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer("taskflow")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .id(java.util.UUID.randomUUID().toString())
                .signWith(signingKey)
                .compact();
    }
}
