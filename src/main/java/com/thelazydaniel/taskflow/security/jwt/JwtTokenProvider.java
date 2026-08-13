package com.thelazydaniel.taskflow.security.jwt;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {


    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long jwtRefreshExpirationMs;

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

        log.debug("Generated JWT Token for username: {}, roles: {}", username, claims);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuer("taskflow")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .id(java.util.UUID.randomUUID().toString())
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuer("taskflow")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtRefreshExpirationMs))
                .id(java.util.UUID.randomUUID().toString())
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date getExpirationFromToken(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("role", List.class);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public Claims getAllClaimsFromToken(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            log.debug("JWT Token validated successfully");

            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT Signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT Token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT Token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public boolean isTokenExpired(String token) {
        try {
            return getExpirationFromToken(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public String getTokenType(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims.get("type", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean canTokenBeRefreshed(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
