package com.ITSA.AdminProxy.model.orchestrator;


import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.ITSA.AdminProxy.model.AuthProvider;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

 @Setter
 @Getter
@AllArgsConstructor
public class User implements OAuth2User{
    public User() {
    }
    private String userId;
    private String name;
    private String firstName;
    private String lastName;
    private String email;
    private String imageUrl;
    private boolean emailVerified;
    private AuthProvider provider;
    private Collection<? extends GrantedAuthority> authorities;
    private String role;
    private boolean isExpired;
    private boolean isLocked;
    private boolean isCredentialsExpired;
    private boolean isEnabled;
    private Instant createdAt;
    private Instant updatedAt;

   @Override
    public Map<String, Object> getAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userId", userId);
        attributes.put("firstName", firstName);
        attributes.put("lastName", lastName);
        attributes.put("email", email);
        attributes.put("imageUrl", imageUrl);
        attributes.put("provider", provider);
        attributes.put("createdAt", createdAt);
        attributes.put("updatedAt", updatedAt);
        attributes.put("emailVerified", emailVerified);
        attributes.put("role", role);
        attributes.put("isExpired", isExpired);
        attributes.put("isLocked", isLocked);
        attributes.put("isCredentialsExpired", isCredentialsExpired);
        attributes.put("isEnabled", isEnabled);
        attributes.put("authorities", authorities);
        return attributes;
    }


public String getProvider() {
return this.provider.toString();   
}

    

   
}
