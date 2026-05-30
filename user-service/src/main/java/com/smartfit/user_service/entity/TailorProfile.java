package com.smartfit.user_service.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tailor_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TailorProfile {
    @Id
    private String id;
    private String userId;
    private String shopName;
    private String address;
    private String phone;
    private String description;
}
