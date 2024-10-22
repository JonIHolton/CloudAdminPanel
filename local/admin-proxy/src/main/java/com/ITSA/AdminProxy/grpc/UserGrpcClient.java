package com.ITSA.AdminProxy.grpc;

import com.ITSA.AdminProxy.model.orchestrator.User;
import com.ITSA.AdminProxy.dto.MultipleUserResponseDTO;

public interface UserGrpcClient {

    User getUserByUserId(String userId);

    User getUserByEmail(String email);

    User createNewUser(User user);

    User updateUser(User user);

    void deleteUser(String userId);

    MultipleUserResponseDTO getAllUsers(int start, int size, String filters, String sorting);

   boolean ping();
}
