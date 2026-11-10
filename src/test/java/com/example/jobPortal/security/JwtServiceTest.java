package com.example.jobPortal.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private final String secret =
            "ThisIsASecretKeyForJwtTesting12345678901234567890";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(
                jwtService,
                "secretKey",
                secret
        );
    }

    @Test
    void generateToken_returnsValidToken() {
        String token = jwtService.generateToken("satwik");

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractUsername_returnsCorrectUsername() {
        String token = jwtService.generateToken("satwik");

        String username = jwtService.extractUsername(token);

        assertEquals("satwik", username);
    }

    @Test
    void isTokenValid_whenUsernameMatches_returnsTrue() {
        String token = jwtService.generateToken("satwik");

        boolean result = jwtService.isTokenValid(token, "satwik");

        assertTrue(result);
    }

    @Test
    void isTokenValid_whenUsernameDoesNotMatch_returnsFalse() {
        String token = jwtService.generateToken("satwik");

        boolean result = jwtService.isTokenValid(token, "otherUser");

        assertFalse(result);
    }

    @Test
    void extractUsername_whenTokenIsInvalid_throwsException() {
        assertThrows(
                Exception.class,
                () -> jwtService.extractUsername("invalid.token.value")
        );
    }

    @Test
    void extractUsername_whenTokenIsExpired_throwsException() {
        SecretKey key = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        String expiredToken = Jwts.builder()
                .subject("satwik")
                .issuedAt(new Date(System.currentTimeMillis() - 120000))
                .expiration(new Date(System.currentTimeMillis() - 60000))
                .signWith(key)
                .compact();

        assertThrows(
                Exception.class,
                () -> jwtService.extractUsername(expiredToken)
        );
    }
}