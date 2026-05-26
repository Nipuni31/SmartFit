package com.smartfit.user_service.dto;

import com.smartfit.user_service.entity.Role;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String name;
    private String gender;
    private Float height;
    private Float weight;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
