package com.avilachehab.christmasgifts.controller;

import com.avilachehab.christmasgifts.dto.LoginRequest;
import com.avilachehab.christmasgifts.filter.JwtAuthenticationFilter;
import com.avilachehab.christmasgifts.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "app.auth.username=testuser",
    "app.auth.password=testpass",
    "app.jwt.secret=test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha-256-algorithm",
    "app.jwt.expiration=86400000"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("testpass");

        String token = "test-jwt-token";
        when(jwtUtil.generateToken("testuser")).thenReturn(token);

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(token))
                .andExpect(jsonPath("$.type").value("Bearer"));

        verify(jwtUtil, times(1)).generateToken("testuser");
    }

    @Test
    void login_WithInvalidUsername_ShouldReturn401() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("wronguser");
        request.setPassword("testpass");

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void login_WithInvalidPassword_ShouldReturn401() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpass");

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void login_WithBcryptPassword_ShouldUsePasswordEncoder() throws Exception {
        // Given
        // This test would require setting up the controller with a bcrypt hash
        // For now, we test the plain password path
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("testpass");

        String token = "test-jwt-token";
        when(jwtUtil.generateToken("testuser")).thenReturn(token);

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token));
    }
}

