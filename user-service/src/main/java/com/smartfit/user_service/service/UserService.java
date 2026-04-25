package com.smartfit.user_service.service;

import com.smartfit.user_service.entity.User;
import lombok.RequiredArgsConstructor;
import com.smartfit.user_service.repository.UserRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    public User register(String username, String password) {
        User user = User.builder()
                .username(username)
                .password(encoder.encode(password))
                .build();

        return userRepository.save(user);
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }

}
