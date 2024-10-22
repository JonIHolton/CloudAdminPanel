package com.ITSA.AdminProxy.dto;


import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

import com.ITSA.AdminProxy.model.orchestrator.User;

@Getter
@Setter
public class UserDTO {
    private String userId;
    private String name;
    private String firstName;
    private String lastName;
    private String email;
    private String imageUrl;
    private String provider; 
    private Instant createdAt;
    private Instant updatedAt;
    private String role;



public static UserDTO convertToDTO(User user) {
    UserDTO dto = new UserDTO();
    dto.setUserId(user.getUserId());
    dto.setName(user.getName());
    dto.setFirstName(user.getFirstName());
    dto.setLastName(user.getLastName());
    dto.setEmail(user.getEmail());
    dto.setImageUrl(user.getImageUrl());
    dto.setProvider(user.getProvider().toString());
    dto.setRole(user.getRole());
    dto.setCreatedAt(user.getCreatedAt());
    dto.setUpdatedAt(user.getUpdatedAt());
    return dto;
}
}