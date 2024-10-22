package com.ITSA.AdminProxy.util;

import lombok.Getter;

@Getter
public enum JwtUtils {
    // This defines the properties that are expected to be present in the JWT token
    USER_ID("userId"),
    NAME("name"),
    SCOPE("scope");

    private String property;

    private JwtUtils(String property) {
        this.property = property;
    }
}
