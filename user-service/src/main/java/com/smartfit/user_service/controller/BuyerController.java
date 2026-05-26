package com.smartfit.user_service.controller;

import org.springframework.web.bind.annotation.*;
import com.smartfit.user_service.dto.UserResponse;
import com.smartfit.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import java.security.Principal;

@RestController
@RequestMapping("/buyer")
@RequiredArgsConstructor
public class BuyerController {

    private final UserService userService;

    @GetMapping("/profile")
    public UserResponse getProfile(Principal principal) {
        try {
            Long userId = Long.parseLong(principal.getName());
            return userService.getUserProfile(userId);
        } catch (Exception e) {
            throw new RuntimeException("Unable to retrieve profile");
        }
    }

    @PutMapping("/profile")
    public Object updateProfile(
            Principal principal,
            @RequestBody UserResponse request) {
        try {
            Long userId = Long.parseLong(principal.getName());
            userService.updateUserProfile(userId, request);
            return java.util.Map.of("message", "Profile updated successfully");
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @PostMapping("/upload-image")
    public Object uploadImage(
            Principal principal,
            @RequestParam String imagePath) {
        try {
            // Store image path in user's uploadedImages list
            return java.util.Map.of("message", "Image uploaded successfully");
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @PostMapping("/save-prediction")
    public Object savePrediction(
            Principal principal,
            @RequestParam String predictionData) {
        try {
            // Store prediction result
            return java.util.Map.of("message", "Prediction saved successfully");
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/view-tailors")
    public Object viewAvailableTailors() {
        try {
            return userService.getAllTailors();
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }
}
