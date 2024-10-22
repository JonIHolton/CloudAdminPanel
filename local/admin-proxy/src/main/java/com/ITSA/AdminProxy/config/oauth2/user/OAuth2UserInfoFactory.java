package com.ITSA.AdminProxy.config.oauth2.user;

import java.util.Map;

import com.ITSA.AdminProxy.exception.OAuth2AuthenticationProcessingException;
import com.ITSA.AdminProxy.model.AuthProvider;

public class OAuth2UserInfoFactory {

    private OAuth2UserInfoFactory() {
    }

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase(AuthProvider.google.toString())) {
            return new GoogleOAuth2UserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationProcessingException(
                    "Sorry! Login with " + registrationId + " is not supported yet.");
        }
    }
}
