package com.smartfit.user_service.service;

import com.smartfit.user_service.entity.User;
import com.smartfit.user_service.entity.Role;
import com.smartfit.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TailorService {

    private final UserRepository userRepository;

    public User getTailorProfile(Long tailorId) {
        User tailor = userRepository.findById(tailorId)
                .orElseThrow(() -> new RuntimeException("Tailor not found"));
        
        if (tailor.getRole() != Role.TAILOR) {
            throw new RuntimeException("User is not a tailor");
        }
        
        return tailor;
    }

    public User updateTailorProfile(Long tailorId, String shopName, String shopAddress, String specialization) {
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
    public User viewCustomerMeasurements(Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        if (customer.getRole() != Role.BUYER) {
            throw new RuntimeException("User is not a customer");
        }
        
        return customer;
    }

    // Store measurement recommendations
    public void addMeasurementRecommendation(Long customerId, String recommendation) {
        // This would be extended with a separate recommendations table
        // For now, it's a placeholder
    }

    // Store tailor's order information
    public void createOrder(Long tailorId, Long customerId, String orderDetails) {
        // This would be extended with a separate orders table
        // For now, it's a placeholder
    }
}
