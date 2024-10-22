package com.ITSA.AdminProxy.config.oauth2.service;
  
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
 
import com.ITSA.AdminProxy.config.oauth2.user.OAuth2UserInfo;
import com.ITSA.AdminProxy.config.oauth2.user.OAuth2UserInfoFactory;
import com.ITSA.AdminProxy.exception.OAuth2AuthenticationProcessingException;
import com.ITSA.AdminProxy.model.AuthProvider;
import com.ITSA.AdminProxy.model.orchestrator.User;
import com.ITSA.AdminProxy.service.UserService;
 
import lombok.RequiredArgsConstructor;
 
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserServiceImpl extends DefaultOAuth2UserService implements CustomOAuth2UserService {
 
    private final UserService  userService;
 
    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
 
        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the
            // OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }
 
    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        try {
            OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                    oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
            if (oAuth2UserInfo.getEmail().isEmpty()) {
                throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
            }
 
            User userOptional = userService.findByEmail(oAuth2UserRequest, oAuth2UserInfo);
            if (userOptional == null) {
                // This line will make the method fail by throwing an exception if userOptional is null
                throw new OAuth2AuthenticationProcessingException("User not found");
            }
            User user;
 
            if (userOptional.getUserId() != null) {
                user = userOptional;
                if(user.getRole().isBlank()) {
                    throw new OAuth2AuthenticationProcessingException("Not Authorised");
                }
                if(user.getRole() == null) {
                    throw new OAuth2AuthenticationProcessingException("Not Authorised");
                }
                if (!user.getProvider()
                        .equals(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()).toString())) {
                    throw new OAuth2AuthenticationProcessingException("Looks like you're signed up with " +
                            user.getProvider() + " account. Please use your " + user.getProvider() +
                            " account to login.");
                }
 
                // Check if user needs updating
                boolean needsUpdate = false;
                String fullName = oAuth2UserInfo.getName();
                String[] nameParts = fullName.split(" ");
                String firstName = nameParts[0];
                String lastName = nameParts[nameParts.length - 1];
                if (!user.getName().equals(fullName) || 
                    !user.getImageUrl().equals(oAuth2UserInfo.getImageUrl()) ||
                    !user.getFirstName().equals(firstName) || 
                    !user.getLastName().equals(lastName)) {
                    needsUpdate = true;
                }
 
                if (needsUpdate) {
                    user = userService.updateExistingUser(user, oAuth2UserInfo);
                }
            } else {
                // If user's getUserId() is null, then also consider as user not found or invalid
                throw new OAuth2AuthenticationProcessingException("Invalid user information");
            }
 
            return user;
        } catch (Exception e) {
            throw new InternalAuthenticationServiceException(e.getMessage(), e.getCause());
        }
    }
 
 
}