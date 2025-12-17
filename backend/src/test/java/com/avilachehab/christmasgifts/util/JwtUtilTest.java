package com.avilachehab.christmasgifts.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

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
    void generateToken_ShouldGenerateValidToken() {
        // Given
        String username = "testuser";

        // When
        String token = jwtUtil.generateToken(username);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    void generateToken_ShouldIncludeUsernameInToken() {
        // Given
        String username = "testuser";

        // When
        String token = jwtUtil.generateToken(username);
        String extractedUsername = jwtUtil.getUsernameFromToken(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    void getUsernameFromToken_ShouldExtractUsername() {
        // Given
        String username = "admin";
        String token = jwtUtil.generateToken(username);

        // When
        String extractedUsername = jwtUtil.getUsernameFromToken(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    void getUsernameFromToken_WithInvalidToken_ShouldThrowException() {
        // Given
        String invalidToken = "invalid.token.here";

        // When/Then
        assertThrows(Exception.class, () -> {
            jwtUtil.getUsernameFromToken(invalidToken);
        });
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Given
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        // When
        boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtUtil.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        // Given
        // Create a token with very short expiration
        ReflectionTestUtils.setField(jwtUtil, "expiration", 1L); // 1ms
        String username = "testuser";
        String token = jwtUtil.generateToken(username);
        
        // Wait for expiration
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertFalse(isValid);
        
        // Reset expiration
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);
    }

    @Test
    void validateToken_WithNullToken_ShouldReturnFalse() {
        // When
        boolean isValid = jwtUtil.validateToken(null);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithEmptyToken_ShouldReturnFalse() {
        // When
        boolean isValid = jwtUtil.validateToken("");

        // Then
        assertFalse(isValid);
    }

    @Test
    void generateToken_WithDifferentUsernames_ShouldGenerateDifferentTokens() {
        // Given
        String username1 = "user1";
        String username2 = "user2";

        // When
        String token1 = jwtUtil.generateToken(username1);
        String token2 = jwtUtil.generateToken(username2);

        // Then
        assertNotEquals(token1, token2);
    }

    @Test
    void generateToken_WithSameUsername_ShouldExtractSameUsername() {
        // Given
        String username = "user1";

        // When
        String token1 = jwtUtil.generateToken(username);
        // Small delay to ensure different issuedAt
        try {
            Thread.sleep(100); // Increased delay to ensure different timestamp
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String token2 = jwtUtil.generateToken(username);

        // Then
        // Both tokens should extract the same username
        assertEquals(username, jwtUtil.getUsernameFromToken(token1));
        assertEquals(username, jwtUtil.getUsernameFromToken(token2));
        // Both tokens should be valid
        assertTrue(jwtUtil.validateToken(token1));
        assertTrue(jwtUtil.validateToken(token2));
    }
}

