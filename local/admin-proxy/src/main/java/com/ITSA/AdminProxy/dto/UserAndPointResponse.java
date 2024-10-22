package com.ITSA.AdminProxy.dto;


import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

import com.ITSA.AdminProxy.model.orchestrator.Points;
import com.ITSA.AdminProxy.model.orchestrator.User;

@Getter
@Setter
public class UserAndPointResponse {
    private String userId;
    private String name;
    private String email;
    private Instant createdAt;
    private String role;
    private List<Points> pointAccount;



public static UserAndPointResponse convertToDTO(User user, List<Points> points) {
    UserAndPointResponse dto = new UserAndPointResponse();
    dto.setUserId(user.getUserId());
    dto.setName(user.getName());
    dto.setEmail(user.getEmail());
    dto.setRole(user.getRole());
    dto.setCreatedAt(user.getCreatedAt());
    dto.setPointAccount(points);
    return dto;
    }
}
