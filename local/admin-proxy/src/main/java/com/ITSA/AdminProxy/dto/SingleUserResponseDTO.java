package com.ITSA.AdminProxy.dto;


import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

import com.ITSA.AdminProxy.model.orchestrator.User;

@Getter
@Setter
public class SingleUserResponseDTO {
    private String userId;
    private String name;
    private String firstName;
    private String lastName;
    private String email;
    private Instant createdAt;
    private String role;



public static SingleUserResponseDTO convertToDTO(User user) {
    SingleUserResponseDTO dto = new SingleUserResponseDTO();
    dto.setUserId(user.getUserId());
    dto.setName(user.getName());
    dto.setEmail(user.getEmail());
    dto.setFirstName(user.getFirstName());
    dto.setLastName(user.getLastName());
    dto.setRole(user.getRole());
    dto.setCreatedAt(user.getCreatedAt());
    return dto;
    }
}
