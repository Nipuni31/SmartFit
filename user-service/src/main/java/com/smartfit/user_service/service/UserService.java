package com.smartfit.user_service.service;

import com.smartfit.user_service.entity.User;
import com.smartfit.user_service.entity.Role;
import com.smartfit.user_service.dto.RegisterRequest;
import com.smartfit.user_service.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import com.smartfit.user_service.repository.UserRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    public User register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .name(request.getName())
                .role(request.getRole() != null ? request.getRole() : Role.BUYER)
                .gender(request.getGender())
                .height(request.getHeight())
                .weight(request.getWeight())
                .shopName(request.getShopName())
                .shopAddress(request.getShopAddress())
                .specialization(request.getSpecialization())
                .build();

        return userRepository.save(user);
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        if (!user.getActive()) {
            throw new RuntimeException("User account is inactive");
        }

        return user;
    }

    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserResponse getUserProfile(String id) {
        User user = getUserById(id);
        return convertToUserResponse(user);
    }

    public User updateUserProfile(String id, UserResponse request) {
        User user = getUserById(id);
        
        if (request.getName() != null) user.setName(request.getName());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getHeight() != null) user.setHeight(request.getHeight());
        if (request.getWeight() != null) user.setWeight(request.getWeight());

        return userRepository.save(user);
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getAllBuyers() {
        return userRepository.findByRole(Role.BUYER).stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getAllTailors() {
        return userRepository.findByRole(Role.TAILOR).stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    public User deactivateUser(String id) {
        User user = getUserById(id);
        user.setActive(false);
        return userRepository.save(user);
    }

    public User activateUser(String id) {
        User user = getUserById(id);
        user.setActive(true);
        return userRepository.save(user);
    }

    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .gender(user.getGender())
                .height(user.getHeight())
                .weight(user.getWeight())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
