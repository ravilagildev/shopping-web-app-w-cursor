package com.avilachehab.christmasgifts.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String TEST_SECRET = "test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha-256-algorithm";
    private static final Long TEST_EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);
    }

    @Test
    @DisplayName("Should generate valid JWT token with three parts")
    void generateToken_validUsername_generatesValidToken() {
        // Arrange
        String username = "testuser";

        // Act
        String token = jwtUtil.generateToken(username);

        // Assert
        assertThat(token)
                .isNotNull()
                .isNotEmpty()
                .contains(".");
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    @DisplayName("Should include username in generated token")
    void generateToken_validUsername_includesUsernameInToken() {
        // Arrange
        String username = "testuser";

        // Act
        String token = jwtUtil.generateToken(username);
        String extractedUsername = jwtUtil.getUsernameFromToken(token);

        // Assert
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    @DisplayName("Should extract username from valid token")
    void getUsernameFromToken_validToken_extractsUsername() {
        // Arrange
        String username = "admin";
        String token = jwtUtil.generateToken(username);

        // Act
        String extractedUsername = jwtUtil.getUsernameFromToken(token);

        // Assert
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    @DisplayName("Should throw exception when extracting username from invalid token")
    void getUsernameFromToken_invalidToken_throwsException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThatThrownBy(() -> jwtUtil.getUsernameFromToken(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should validate correctly formed token")
    void validateToken_validToken_returnsTrue() {
        // Arrange
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        // Act
        boolean isValid = jwtUtil.validateToken(token);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject malformed token")
    void validateToken_malformedToken_returnsFalse() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtUtil.validateToken(invalidToken);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject expired token")
    void validateToken_expiredToken_returnsFalse() {
        // Arrange - Create an already expired token directly
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date(System.currentTimeMillis() - 100000)) // Issued in the past
                .expiration(new Date(System.currentTimeMillis() - 1000)) // Expired 1 second ago
                .signWith(key)
                .compact();

        // Act
        boolean isValid = jwtUtil.validateToken(expiredToken);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return false for null token")
    void validateToken_nullToken_returnsFalse() {
        // Act
        boolean isValid = jwtUtil.validateToken(null);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return false for empty token")
    void validateToken_emptyToken_returnsFalse() {
        // Act
        boolean isValid = jwtUtil.validateToken("");

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should generate different tokens for different usernames")
    void generateToken_differentUsernames_generatesDifferentTokens() {
        // Arrange
        String username1 = "user1";
        String username2 = "user2";

        // Act
        String token1 = jwtUtil.generateToken(username1);
        String token2 = jwtUtil.generateToken(username2);

        // Assert
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("Should extract same username from tokens generated at different times")
    void generateToken_sameUsernameDifferentTimes_extractsSameUsername() {
        // Arrange
        String username = "user1";

        // Act
        String token1 = jwtUtil.generateToken(username);
        String token2 = jwtUtil.generateToken(username);

        // Assert
        assertThat(jwtUtil.getUsernameFromToken(token1)).isEqualTo(username);
        assertThat(jwtUtil.getUsernameFromToken(token2)).isEqualTo(username);
        assertThat(jwtUtil.validateToken(token1)).isTrue();
        assertThat(jwtUtil.validateToken(token2)).isTrue();
    }

    @Test
    @DisplayName("Should reject token signed with different secret")
    void validateToken_differentSecret_returnsFalse() {
        // Arrange - Create token with different secret
        String differentSecret = "different-secret-key-for-testing-purposes-must-be-long-enough-for-hmac-sha256";
        SecretKey differentKey = Keys.hmacShaKeyFor(differentSecret.getBytes(StandardCharsets.UTF_8));
        String tokenWithDifferentSecret = Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(differentKey)
                .compact();

        // Act
        boolean isValid = jwtUtil.validateToken(tokenWithDifferentSecret);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should handle special characters in username")
    void generateToken_usernameWithSpecialChars_generatesValidToken() {
        // Arrange
        String username = "user@example.com";

        // Act
        String token = jwtUtil.generateToken(username);
        String extractedUsername = jwtUtil.getUsernameFromToken(token);

        // Assert
        assertThat(extractedUsername).isEqualTo(username);
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }
}
