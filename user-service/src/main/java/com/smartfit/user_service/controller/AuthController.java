package com.smartfit.user_service.controller;

import org.springframework.web.bind.annotation.*;

import com.smartfit.user_service.entity.User;
import com.smartfit.user_service.dto.LoginRequest;
import com.smartfit.user_service.dto.RegisterRequest;
import com.smartfit.user_service.dto.JwtResponse;
import com.smartfit.user_service.security.JwtUtil;
import com.smartfit.user_service.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public JwtResponse register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().toString());
            
            return JwtResponse.builder()
                    .token(token)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .name(user.getName())
                    .message("User registered successfully")
                    .build();
        } catch (RuntimeException e) {
            return JwtResponse.builder()
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }

    @PostMapping("/login")
    public JwtResponse login(@RequestBody LoginRequest request) {
        try {
            User user = userService.login(request.getUsername(), request.getPassword());
            String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().toString());
            
            return JwtResponse.builder()
                    .token(token)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .name(user.getName())
                    .message("Login successful")
                    .build();
        } catch (RuntimeException e) {
            return JwtResponse.builder()
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }

    @PostMapping("/validate")
    public Object validateToken(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return java.util.Map.of("valid", false, "message", "Invalid token format");
        }

        String jwt = token.substring(7);
        boolean isValid = JwtUtil.validateToken(jwt);
        
        if (isValid) {
            return java.util.Map.of(
                    "valid", true,
                    "username", JwtUtil.extractUsername(jwt),
                    "role", JwtUtil.extractRole(jwt),
                    "userId", JwtUtil.extractUserId(jwt)
            );
        } else {
            return java.util.Map.of("valid", false, "message", "Invalid or expired token");
        }
    }
}
