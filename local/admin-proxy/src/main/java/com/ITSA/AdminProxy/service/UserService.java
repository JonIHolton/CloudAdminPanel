package com.ITSA.AdminProxy.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;

import com.ITSA.AdminProxy.config.oauth2.user.OAuth2UserInfo;

import com.ITSA.AdminProxy.model.orchestrator.User;
import com.ITSA.AdminProxy.dto.UserApiResponse;

public interface UserService extends UserDetailsService{

    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    User registerNewUser(User user);

    User updateUser(User user);

    void deleteUser(String userId);

    User loadUserById(String userId);

    User findById(String userId);

    User findByEmail(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo);

    User updateExistingUser(User user, OAuth2UserInfo oAuth2UserInfo);

    UserApiResponse getAllUsers(int start, int size, String filters, String sorting);
}