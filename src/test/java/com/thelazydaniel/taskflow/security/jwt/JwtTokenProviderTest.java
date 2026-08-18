package com.thelazydaniel.taskflow.security.jwt;

import io.jsonwebtoken.Claims;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;


import java.util.List;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private final String testSecret = "c2VjcmV0S2V5Rm9yVGVzdGluZ1B1cnBvc2VzT25seU5vdFVzZWRJblByb2R1Y3Rpb24=";
    private final long expirationMs = 3600000; // 1 hour
    private final long refreshExpirationMs = 86400000; // 24 hours

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();

        // Set private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", expirationMs);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtRefreshExpirationMs", refreshExpirationMs);
    }

    @Test
    void generateTokenFromUsername_ShouldCreateValidToken() {
        // Arrange
        String username = "testuser";
        List<String> authorities = List.of("ROLE_USER", "ROLE_ADMIN");

        // Act
        String token = jwtTokenProvider.generateTokenFromUsername(username, authorities);

        // Assert
        assertThat(token).isNotNull().isNotEmpty();

        // Verify token contains correct username
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);
        assertThat(extractedUsername).isEqualTo(username);

        // Verify token contains correct roles
        List<String> extractedRoles = jwtTokenProvider.getRolesFromToken(token);
        assertThat(extractedRoles).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void generateRefreshToken_ShouldCreateValidRefreshToken() {
        // Arrange
        String username = "testuser";

        // Act
        String token = jwtTokenProvider.generateRefreshToken(username);

        // Assert
        assertThat(token).isNotNull().isNotEmpty();

        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    void generateJwtToken_ShouldCreateTokenFromAuthentication() {
        // Arrange
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                )
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // Act
        String token = jwtTokenProvider.generateJwtToken(authentication);

        // Assert
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo("testuser");
        assertThat(jwtTokenProvider.getRolesFromToken(token))
                .contains("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void generateJwtToken_ShouldThrowException_WhenAuthenticationNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            jwtTokenProvider.generateJwtToken(null);
        });
    }

    @Test
    void generateJwtToken_ShouldThrowException_WhenPrincipalNull() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            jwtTokenProvider.generateJwtToken(authentication);
        });
    }

    @Test
    void generateJwtToken_ShouldThrowException_WhenPrincipalNotUserDetails() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("not a UserDetails");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            jwtTokenProvider.generateJwtToken(authentication);
        });
    }

    @Test
    void getUsernameFromToken_ShouldReturnCorrectUsername() {
        // Arrange
        String username = "testuser";
        String token = jwtTokenProvider.generateTokenFromUsername(username, List.of("ROLE_USER"));

        // Act
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    void getRolesFromToken_ShouldReturnCorrectRoles() {
        // Arrange
        List<String> expectedRoles = List.of("ROLE_USER", "ROLE_ADMIN", "ROLE_MANAGER");
        String token = jwtTokenProvider.generateTokenFromUsername("testuser", expectedRoles);

        // Act
        List<String> extractedRoles = jwtTokenProvider.getRolesFromToken(token);

        // Assert
        assertThat(extractedRoles).containsExactlyInAnyOrderElementsOf(expectedRoles);
    }

    @Test
    void validateToken_ShouldReturnTrue_ForValidToken() {
        // Arrange
        String token = jwtTokenProvider.generateTokenFromUsername("testuser", List.of("ROLE_USER"));

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_ShouldReturnFalse_ForInvalidToken() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("invalid.token.here");

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_ShouldReturnFalse_ForMalformedToken() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("malformed-token");

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenExpired_ShouldReturnFalse_ForNewToken() {
        // Arrange
        String token = jwtTokenProvider.generateTokenFromUsername("testuser", List.of("ROLE_USER"));

        // Act
        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        // Assert
        assertThat(isExpired).isFalse();
    }

    @Test
    void getExpirationFromToken_ShouldReturnFutureDate() {
        // Arrange
        String token = jwtTokenProvider.generateTokenFromUsername("testuser", List.of("ROLE_USER"));

        // Act
        Date expiration = jwtTokenProvider.getExpirationFromToken(token);

        // Assert
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    void getTokenType_ShouldReturnNull_WhenTypeNotPresent() {
        // Arrange
        String token = jwtTokenProvider.generateTokenFromUsername("testuser", List.of("ROLE_USER"));

        // Act
        String tokenType = jwtTokenProvider.getTokenType(token);

        // Assert
        assertThat(tokenType).isNull();
    }

    @Test
    void canTokenBeRefreshed_ShouldReturnTrue_ForValidToken() {
        // Arrange
        String token = jwtTokenProvider.generateTokenFromUsername("testuser", List.of("ROLE_USER"));

        // Act
        boolean canRefresh = jwtTokenProvider.canTokenBeRefreshed(token);

        // Assert
        assertThat(canRefresh).isTrue();
    }

    @Test
    void extractClaim_ShouldExtractCorrectClaim() {
        // Arrange
        String token = jwtTokenProvider.generateTokenFromUsername("testuser", List.of("ROLE_USER"));

        // Act
        String subject = jwtTokenProvider.extractClaim(token, Claims::getSubject);

        // Assert
        assertThat(subject).isEqualTo("testuser");
    }

    @Test
    void getAllClaimsFromToken_ShouldReturnAllClaims() {
        // Arrange
        String token = jwtTokenProvider.generateTokenFromUsername("testuser", List.of("ROLE_USER"));

        // Act
        Claims claims = jwtTokenProvider.getAllClaimsFromToken(token);

        // Assert
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo("testuser");
        assertThat(claims.get("role", List.class)).contains("ROLE_USER");
        assertThat(claims.getIssuer()).isEqualTo("taskflow");
    }
}