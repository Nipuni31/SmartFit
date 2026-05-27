package com.smartfit.user_service.dto;

import com.smartfit.user_service.entity.Role;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TailorResponse {
    private String id;
    private String username;
    private String email;
    private String name;
    private Role role;
    private String shopName;
    private String shopAddress;
    private String specialization;
    private Instant createdAt;
}
