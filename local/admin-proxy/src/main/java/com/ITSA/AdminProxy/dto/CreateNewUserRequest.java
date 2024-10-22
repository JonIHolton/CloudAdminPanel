package com.ITSA.AdminProxy.dto;


import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class CreateNewUserRequest {
    private String name;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
}
