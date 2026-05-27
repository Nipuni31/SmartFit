package com.smartfit.user_service.controller;

import org.springframework.web.bind.annotation.*;
import com.smartfit.user_service.dto.TailorResponse;
import com.smartfit.user_service.entity.User;
import com.smartfit.user_service.service.TailorService;
import com.smartfit.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/tailor")
@RequiredArgsConstructor
public class TailorController {

    private final TailorService tailorService;
    private final UserService userService;

    @GetMapping("/profile")
    public Object getProfile(Principal principal) {
        try {
            String tailorId = principal.getName();
            User tailor = tailorService.getTailorProfile(tailorId);
            return convertToTailorResponse(tailor);
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @PutMapping("/profile")
    public Object updateProfile(
            Principal principal,
            @RequestBody TailorResponse request) {
        try {
            String tailorId = principal.getName();
            User tailor = tailorService.updateTailorProfile(
                    tailorId,
                    request.getShopName(),
                    request.getShopAddress(),
                    request.getSpecialization()
            );
            return java.util.Map.of("message", "Profile updated successfully", "tailor", convertToTailorResponse(tailor));
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/customers")
    public Object getCustomersList() {
        try {
            return userService.getAllBuyers();
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/customer/{customerId}/measurements")
    public Object viewCustomerMeasurements(
            @PathVariable String customerId,
            Principal principal) {
        try {
            User customer = tailorService.viewCustomerMeasurements(customerId);
            return java.util.Map.of(
                    "customerId", customer.getId(),
                    "name", customer.getName(),
                    "gender", customer.getGender(),
                    "height", customer.getHeight(),
                    "weight", customer.getWeight()
            );
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @PostMapping("/recommend/{customerId}")
    public Object recommendClothing(
            @PathVariable String customerId,
            @RequestBody java.util.Map<String, String> request,
            Principal principal) {
        try {
            String recommendation = request.get("recommendation");
            tailorService.addMeasurementRecommendation(customerId, recommendation);
            return java.util.Map.of("message", "Recommendation saved successfully");
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @PostMapping("/order/create")
    public Object createOrder(
            @RequestBody java.util.Map<String, Object> request,
            Principal principal) {
        try {
            String tailorId = principal.getName();
            String customerId = request.get("customerId").toString();
            String orderDetails = (String) request.get("orderDetails");
            
            tailorService.createOrder(tailorId, customerId, orderDetails);
            return java.util.Map.of("message", "Order created successfully");
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/all")
    public Object getAllTailors() {
        try {
            List<User> tailors = tailorService.getAllTailors();
            return tailors.stream().map(this::convertToTailorResponse).toList();
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/search")
    public Object searchTailors(@RequestParam String specialization) {
        try {
            List<User> tailors = tailorService.searchTailorsBySpecialization(specialization);
            return tailors.stream().map(this::convertToTailorResponse).toList();
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    private TailorResponse convertToTailorResponse(User tailor) {
        return TailorResponse.builder()
                .id(tailor.getId())
                .username(tailor.getUsername())
                .email(tailor.getEmail())
                .name(tailor.getName())
                .role(tailor.getRole())
                .shopName(tailor.getShopName())
                .shopAddress(tailor.getShopAddress())
                .specialization(tailor.getSpecialization())
                .createdAt(tailor.getCreatedAt())
                .build();
    }
}
