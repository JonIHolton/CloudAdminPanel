package com.ITSA.AdminProxy.dto;

import com.ITSA.AdminProxy.model.orchestrator.User;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MultipleUserResponseDTO {
    private List<User> users;
    private int totalUsers;

    public MultipleUserResponseDTO(List<User> users, int totalUsers) {
        this.users = users;
        this.totalUsers = totalUsers;
    }
}
