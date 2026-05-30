package com.smartfit.user_service.dto;

import com.smartfit.user_service.entity.Role;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String name;
    private Role role;
    private String gender;
    private Float height;
    private Float weight;
    private String phone;
    private String description;
    // Tailor specific fields
    private String shopName;
    private String shopAddress;
    private String specialization;
}
