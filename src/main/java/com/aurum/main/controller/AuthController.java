package com.aurum.main.controller;

import com.aurum.main.dto.requests.EmailRequest;
import com.aurum.main.dto.responses.AuthResponse;
import com.aurum.main.dto.responses.OtpResponse;
import com.aurum.main.service.AuthService;
import com.aurum.main.dto.requests.LoginRequest;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Data
@RequestMapping(path = "/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping(path = "/verify")
    public ResponseEntity<OtpResponse> verifyLoginRequest(@Valid @RequestBody EmailRequest request) {
        return ResponseEntity.ok(authService.verifyLoginRequest(request));
    }

    @PostMapping(path = "/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
