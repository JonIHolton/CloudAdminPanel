package com.ITSA.AdminProxy.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateRequest {
    private String name;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
}