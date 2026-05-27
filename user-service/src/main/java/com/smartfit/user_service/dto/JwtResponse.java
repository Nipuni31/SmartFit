package com.smartfit.user_service.dto;

import com.smartfit.user_service.entity.Role;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String userId;
    private String username;
    private String email;
    private Role role;
    private String name;
    private String message;
}
