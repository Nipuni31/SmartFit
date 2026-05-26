package com.smartfit.user_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;
    
    @Column(unique = true)
    private String email;
    
    private String password;
    private String name;
    private String gender;
    private Float height; // in cm
    private Float weight; // in kg
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    // For Tailor specific info
    private String shopName;
    private String shopAddress;
    private String specialization; // e.g., formal wear, casual wear, traditional
    
    // User activity tracking
    @ElementCollection
    private List<String> uploadedImages; // store image paths
    
    @ElementCollection
    private List<String> savedPredictions; // store prediction results
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
