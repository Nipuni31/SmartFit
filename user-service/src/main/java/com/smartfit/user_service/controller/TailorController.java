package com.smartfit.user_service.controller;

import org.springframework.web.bind.annotation.*;
import com.smartfit.user_service.dto.CreateOrderRequest;
import com.smartfit.user_service.dto.ManualMeasurementRequest;
import com.smartfit.user_service.dto.TailorResponse;
import com.smartfit.user_service.entity.Measurement;
import com.smartfit.user_service.entity.Order;
import com.smartfit.user_service.entity.User;
import com.smartfit.user_service.service.TailorService;
import com.smartfit.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import java.security.Principal;
import java.util.List;
import java.util.Map;

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
            List<Measurement> measurements = tailorService.getCustomerMeasurements(customerId);
            return java.util.Map.of(
                    "customerId", customerId,
                    "measurements", measurements
            );
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @PostMapping("/customer/{customerId}/measurements")
    public Object addCustomerMeasurement(
            @PathVariable String customerId,
            @RequestBody ManualMeasurementRequest request,
            Principal principal) {
        try {
            Measurement measurement = tailorService.addManualMeasurement(customerId, request);
            return java.util.Map.of("message", "Measurement saved successfully", "measurement", measurement);
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/customer/{customerId}/history")
    public Object viewCustomerHistory(
            @PathVariable String customerId,
            Principal principal) {
        try {
            Map<String, Object> history = tailorService.getCustomerHistory(customerId);
            return history;
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/customer/{customerId}/predictions")
    public Object viewCustomerPredictions(
            @PathVariable String customerId,
            Principal principal) {
        try {
            return tailorService.getCustomerPredictions(customerId);
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
            @RequestBody CreateOrderRequest request,
            Principal principal) {
        try {
            String tailorId = principal.getName();
            tailorService.createOrder(tailorId, request.getCustomerId(), request.getOrderDetails());
            return java.util.Map.of("message", "Order created successfully");
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/orders")
    public Object getOrders(Principal principal) {
        try {
            String tailorId = principal.getName();
            List<Order> orders = tailorService.getOrdersForTailor(tailorId);
            return orders;
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    @PostMapping("/order/{orderId}/status")
    public Object updateOrderStatus(
            @PathVariable String orderId,
            @RequestBody java.util.Map<String, String> request,
            Principal principal) {
        try {
            String status = request.get("status");
            Order updated = tailorService.updateOrderStatus(orderId, status);
            return java.util.Map.of("message", "Order status updated", "order", updated);
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
