package com.smartfit.user_service.service;

import com.smartfit.user_service.dto.ManualMeasurementRequest;
import com.smartfit.user_service.entity.Measurement;
import com.smartfit.user_service.entity.Order;
import com.smartfit.user_service.entity.User;
import com.smartfit.user_service.entity.Role;
import com.smartfit.user_service.repository.MeasurementRepository;
import com.smartfit.user_service.repository.OrderRepository;
import com.smartfit.user_service.repository.PredictionRepository;
import com.smartfit.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TailorService {

    private final UserRepository userRepository;
    private final MeasurementRepository measurementRepository;
    private final PredictionRepository predictionRepository;
    private final OrderRepository orderRepository;

    public User getTailorProfile(String tailorId) {
        User tailor = userRepository.findById(tailorId)
                .orElseThrow(() -> new RuntimeException("Tailor not found"));
        
        if (tailor.getRole() != Role.TAILOR) {
            throw new RuntimeException("User is not a tailor");
        }
        
        return tailor;
    }

    public User updateTailorProfile(String tailorId, String shopName, String shopAddress, String specialization) {
        User tailor = getTailorProfile(tailorId);
        
        if (shopName != null) tailor.setShopName(shopName);
        if (shopAddress != null) tailor.setShopAddress(shopAddress);
        if (specialization != null) tailor.setSpecialization(specialization);

        return userRepository.save(tailor);
    }

    public List<User> getAllTailors() {
        return userRepository.findByRole(Role.TAILOR);
    }

    public List<User> searchTailorsBySpecialization(String specialization) {
        return userRepository.findByRole(Role.TAILOR).stream()
                .filter(tailor -> tailor.getSpecialization() != null 
                        && tailor.getSpecialization().toLowerCase().contains(specialization.toLowerCase()))
                .collect(Collectors.toList());
    }

    // View customer measurements
    public User viewCustomerMeasurements(String customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        if (customer.getRole() != Role.BUYER) {
            throw new RuntimeException("User is not a customer");
        }
        
        return customer;
    }

    // Store measurement recommendations
    public void addMeasurementRecommendation(String customerId, String recommendation) {
        // This would be extended with a separate recommendations table
        // For now, it's a placeholder
    }

    public List<Measurement> getCustomerMeasurements(String customerId) {
        return measurementRepository.findByUserIdOrderByCreatedAtDesc(customerId);
    }

    public Measurement addManualMeasurement(String customerId, ManualMeasurementRequest request) {
        Measurement measurement = Measurement.builder()
                .userId(customerId)
                .height(request.getHeight())
                .shoulder(request.getShoulder())
                .waist(request.getWaist())
                .hip(request.getHip())
                .chestDepth(request.getChestDepth())
                .hipDepth(request.getHipDepth())
                .build();
        return measurementRepository.save(measurement);
    }

    public void createOrder(String tailorId, String customerId, String orderDetails) {
        Order order = Order.builder()
                .tailorId(tailorId)
                .buyerId(customerId)
                .details(orderDetails)
                .status("Pending")
                .build();
        orderRepository.save(order);
    }

    public List<Order> getOrdersForTailor(String tailorId) {
        return orderRepository.findByTailorIdOrderByCreatedAtDesc(tailorId);
    }

    public Order updateOrderStatus(String orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public java.util.Map<String, Object> getCustomerHistory(String customerId) {
        User customer = viewCustomerMeasurements(customerId);
        List<Measurement> measurements = getCustomerMeasurements(customerId);
        List<com.smartfit.user_service.entity.Prediction> predictions = predictionRepository.findByUserIdOrderByCreatedAtDesc(customerId);
        return java.util.Map.of(
                "customerId", customer.getId(),
                "name", customer.getName(),
                "gender", customer.getGender(),
                "height", customer.getHeight(),
                "weight", customer.getWeight(),
                "measurements", measurements,
                "predictions", predictions
        );
    }

    public List<com.smartfit.user_service.entity.Prediction> getCustomerPredictions(String customerId) {
        return predictionRepository.findByUserIdOrderByCreatedAtDesc(customerId);
    }
}
