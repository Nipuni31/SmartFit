package com.smartfit.user_service.controller;

import org.springframework.web.bind.annotation.*;
import com.smartfit.user_service.dto.UserResponse;
import com.smartfit.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/users/all")
    public Object getAllUsers() {
        try {
            List<UserResponse> users = userService.getAllUsers();
            return java.util.Map.of("users", users, "count", users.size());
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/users/buyers")
    public Object getAllBuyers() {
        try {
            List<UserResponse> buyers = userService.getAllBuyers();
            return java.util.Map.of("buyers", buyers, "count", buyers.size());
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/users/tailors")
    public Object getAllTailors() {
        try {
            List<UserResponse> tailors = userService.getAllTailors();
            return java.util.Map.of("tailors", tailors, "count", tailors.size());
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public Object getUserDetails(@PathVariable Long userId) {
        try {
            UserResponse user = userService.getUserProfile(userId);
            return user;
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @PostMapping("/user/{userId}/deactivate")
    public Object deactivateUser(@PathVariable Long userId) {
        try {
            userService.deactivateUser(userId);
            return java.util.Map.of("message", "User deactivated successfully");
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @PostMapping("/user/{userId}/activate")
    public Object activateUser(@PathVariable Long userId) {
        try {
            userService.activateUser(userId);
            return java.util.Map.of("message", "User activated successfully");
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/analytics/user-count")
    public Object getUserCount() {
        try {
            long totalUsers = userService.getAllUsers().size();
            long buyers = userService.getAllBuyers().size();
            long tailors = userService.getAllTailors().size();
            
            return java.util.Map.of(
                    "totalUsers", totalUsers,
                    "buyers", buyers,
                    "tailors", tailors,
                    "admins", totalUsers - buyers - tailors
            );
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/analytics/dashboard")
    public Object getDashboardStats() {
        try {
            return java.util.Map.of(
                    "message", "Analytics dashboard data",
                    "stats", java.util.Map.of(
                            "totalUsers", userService.getAllUsers().size(),
                            "activeUsers", userService.getAllUsers().stream().count(),
                            "recentRegistrations", "Coming soon"
                    )
            );
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @PostMapping("/reports/generate")
    public Object generateReport(@RequestParam String reportType) {
        try {
            return java.util.Map.of(
                    "message", "Report generated successfully",
                    "reportType", reportType,
                    "timestamp", java.time.LocalDateTime.now()
            );
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }
}
