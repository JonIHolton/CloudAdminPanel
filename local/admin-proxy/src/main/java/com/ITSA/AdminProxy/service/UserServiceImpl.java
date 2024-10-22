package com.ITSA.AdminProxy.service;

import com.ITSA.AdminProxy.config.oauth2.user.OAuth2UserInfo;
import com.ITSA.AdminProxy.grpc.UserGrpcClient;
import com.ITSA.AdminProxy.model.orchestrator.User;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.stereotype.Service;
import com.ITSA.AdminProxy.dto.SingleUserResponseDTO;
import com.ITSA.AdminProxy.dto.MultipleUserResponseDTO;
import com.ITSA.AdminProxy.dto.UserApiResponse;
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserGrpcClient userGrpcClient;

    @Autowired
    public UserServiceImpl(UserGrpcClient userGrpcClient) {
        this.userGrpcClient = userGrpcClient;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
           User user = userGrpcClient.getUserByEmail(username);
            if (user == null) {
                throw new UsernameNotFoundException("User not found with email: " + username);
            }
            // Assuming there's a method to convert User to a UserDetails object
            return convertToUserDetails(user);
        } catch (Exception e) {
            logger.error("Error occurred while loading user by username: {}", username, e);
            throw new UsernameNotFoundException("Failed to load user by email: " + username, e);
        }
    }

    @Override
    public User registerNewUser(User user) {
        try {
            return userGrpcClient.createNewUser(user);
        } catch (Exception e) {
            logger.error("Error occurred while registering new user", e);
            throw e; // Custom exception handling as per your application's needs
        }
    }

    @Override
    public User updateUser(User user) {
        try {
            return userGrpcClient.updateUser(user);
        } catch (Exception e) {
            logger.error("Error occurred while updating user: {}", user.getUserId(), e);
            throw e; // Custom exception handling as per your application's needs
        }
    }

    @Override
    public void deleteUser(String userId) {
        try {
            userGrpcClient.deleteUser(userId);
        } catch (Exception e) {
            logger.error("Error occurred while deleting user: {}", userId, e);
            throw e; // Custom exception handling as per your application's needs
        }
    }

    @Override
    public User loadUserById(String userId) {
        try {
            return userGrpcClient.getUserByUserId(userId);
        } catch (Exception e) {
            logger.error("Error occurred while loading user by ID: {}", userId, e);
            throw new UsernameNotFoundException("Failed to load user by ID: " + userId, e);
        }
    }

    // Utility method to convert User to UserDetails
    private UserDetails convertToUserDetails(User user) {
        // Implementation of converting User to UserDetails goes here
        // This typically involves setting username, password (if any), authorities, etc.
        throw new UnsupportedOperationException("Method convertToUserDetails not implemented");
    }

    @Override
    public User findById(String userId) {
        try {
            return userGrpcClient.getUserByUserId(userId);
        } catch (Exception e) {
            logger.error("Error occurred while registering new user", e);
            throw e; // Custom exception handling as per your application's needs
        }
    }

    @Override
    public User findByEmail(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        try {
            String email = oAuth2UserInfo.getEmail();
            User user = userGrpcClient.getUserByEmail(email);
            if(user != null ){
                // this is based on userData.ts in frotnend
                User userToReturn = new User();
                userToReturn.setEmail(user.getEmail());
                userToReturn.setFirstName(user.getFirstName());
                userToReturn.setLastName(user.getLastName());
                // userToReturn.setName(user.getName());
                userToReturn.setImageUrl(user.getImageUrl());
                userToReturn.setRole(user.getRole());
                userToReturn.setUserId(user.getUserId());
                return user;
            } else {
                throw new UsernameNotFoundException(email);
            }
        } catch (Exception e) {
            logger.error("User not found", oAuth2UserInfo.getEmail());
            return null;            
        }
    }

    @Override
    public User updateExistingUser(User user, OAuth2UserInfo oAuth2UserInfo) {
        // This function is usually triggered when oauth2 info returns info that is different than stored in db
        // for things like image url that a created admin cannot implement manually
        String fullName = oAuth2UserInfo.getName();
        String[] nameParts = fullName.split(" ");

        String firstName = nameParts[0];
        String lastName = nameParts[nameParts.length - 1];
        user.setImageUrl(oAuth2UserInfo.getImageUrl());
        user.setName(oAuth2UserInfo.getName());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setCredentialsExpired(false); // we do this because creds refresdhed
        return user;
    }

    @Override
    public UserApiResponse getAllUsers(int start, int size, String filters, String sorting) {
        // Call gRPC serviceimpl
        try {
            MultipleUserResponseDTO usersResponse = userGrpcClient.getAllUsers(start, size, filters, sorting);
            List<SingleUserResponseDTO> userDTOList = new ArrayList<>();
            // for each user in the lsit conver it to a DTO
            for (User user : usersResponse.getUsers()) {
                SingleUserResponseDTO userDTO = SingleUserResponseDTO.convertToDTO(user);
                userDTOList.add(userDTO);
            }
            return new UserApiResponse(userDTOList, usersResponse.getTotalUsers());
        } catch (Exception e) {
            logger.error("Error occurred while registering new user", e);
            throw e; // Custom exception handling as per your application's needs
        }
    }
}
