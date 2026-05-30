package com.smartfit.user_service.controller;

import org.springframework.web.bind.annotation.*;
import com.smartfit.user_service.dto.PredictionRequest;
import com.smartfit.user_service.dto.UserResponse;
import com.smartfit.user_service.entity.Prediction;
import com.smartfit.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/buyer")
@RequiredArgsConstructor
public class BuyerController {

    private final UserService userService;

    @GetMapping("/profile")
    public UserResponse getProfile(Principal principal) {
        try {
            String userId = principal.getName();
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
            String userId = principal.getName();
            userService.updateUserProfile(userId, request);
            return java.util.Map.of("message", "Profile updated successfully");
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @PostMapping("/upload-image")
    public Object uploadImage(
            Principal principal,
            @RequestBody Map<String, Object> body) {
        try {
            // Store image path or metadata in the user profile if desired
            return java.util.Map.of("message", "Image uploaded successfully");
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @PostMapping("/save-prediction")
    public Object savePrediction(
            Principal principal,
            @RequestBody PredictionRequest request) {
        try {
            userService.savePrediction(principal.getName(), request);
            return java.util.Map.of("message", "Prediction saved successfully");
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/predictions")
    public List<Prediction> getPredictionHistory(Principal principal) {
        return userService.getPredictionsForUser(principal.getName());
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
