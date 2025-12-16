package com.avilachehab.christmasgifts.controller;

import com.avilachehab.christmasgifts.dto.LoginRequest;
import com.avilachehab.christmasgifts.dto.LoginResponse;
import com.avilachehab.christmasgifts.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.auth.username}")
    private String configuredUsername;

    @Value("${app.auth.password}")
    private String configuredPassword;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        boolean usernameMatches = request.getUsername().equals(configuredUsername);

        boolean passwordMatches;
        if (configuredPassword.startsWith("$2")) { // allow providing a bcrypt hash
            passwordMatches = passwordEncoder.matches(request.getPassword(), configuredPassword);
        } else {
            passwordMatches = request.getPassword().equals(configuredPassword);
        }

        if (usernameMatches && passwordMatches) {
            String token = jwtUtil.generateToken(request.getUsername());
            return ResponseEntity.ok(new LoginResponse(token, "Bearer"));
        }

        return ResponseEntity.status(401).build();
    }
}

