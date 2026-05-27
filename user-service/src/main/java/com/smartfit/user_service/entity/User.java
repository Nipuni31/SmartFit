package com.smartfit.user_service.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.*;
import java.util.List;
import java.time.Instant;

@Document(collection = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    @Indexed(unique = true)
    private String email;

    private String password;
    private String name;
    private String gender;
    private Float height; // in cm
    private Float weight; // in kg

    private Role role;

    // For Tailor specific info
    private String shopName;
    private String shopAddress;
    private String specialization; // e.g., formal wear, casual wear, traditional

    // User activity tracking
    private List<String> uploadedImages; // store image paths
    private List<String> savedPredictions; // store prediction results

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private Boolean active = true;

}
