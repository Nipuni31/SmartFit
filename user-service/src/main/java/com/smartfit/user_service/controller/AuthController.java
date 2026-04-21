package com.smartfit.user_service.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartfit.user_service.entity.User;
import com.smartfit.user_service.security.JwtUtil;
import com.smartfit.user_service.service.UserService;


import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor

public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public User register(@RequestBody User request) {
        return userService.register(request.getUsername(), request.getPassword());
    }

    @PostMapping("/login")
    public String login(@RequestBody User request) {
        User user = userService.login(request.getUsername(), request.getPassword());
        return JwtUtil.generateToken(user.getUsername());
    }

}
