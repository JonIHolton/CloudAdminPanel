package com.ITSA.AdminProxy.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

import com.ITSA.AdminProxy.model.orchestrator.Points;
// import com.ITSA.AdminProxy.model.orchestrator.User;

@Getter
@Setter
public class PointResponse {
    private String userId;
    private String name;
    private String email;
    private Instant createdAt;
    private String role;
    private List<Points> pointAccount;


}
