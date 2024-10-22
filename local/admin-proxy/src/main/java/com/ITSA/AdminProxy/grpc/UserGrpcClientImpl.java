package com.ITSA.AdminProxy.grpc;

import com.ITSA.AdminProxy.dto.MultipleUserResponseDTO;
import com.ITSA.AdminProxy.model.AuthProvider;
import com.ITSA.AdminProxy.model.orchestrator.User;
import com.ITSA.AdminProxy.userorchestrator.UserManagementServiceGrpc;
import com.ITSA.AdminProxy.userorchestrator.UserOrchestrator;
import com.ITSA.AdminProxy.userorchestrator.UserOrchestrator.CreateUserRequest;
import com.ITSA.AdminProxy.userorchestrator.UserOrchestrator.CreateUserResponse;
import com.ITSA.AdminProxy.userorchestrator.UserOrchestrator.GetAllUsersRequest;
import com.ITSA.AdminProxy.userorchestrator.UserOrchestrator.GetAllUsersResponse;
import com.ITSA.AdminProxy.userorchestrator.UserOrchestrator.GetRequest;
import com.ITSA.AdminProxy.userorchestrator.UserOrchestrator.PingRequest;
import com.ITSA.AdminProxy.userorchestrator.UserOrchestrator.PingResponse;
import com.ITSA.AdminProxy.userorchestrator.UserOrchestrator.UpdateUserRequest;
import com.ITSA.AdminProxy.userorchestrator.UserOrchestrator.UpdateUserResponse;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class UserGrpcClientImpl implements UserGrpcClient {

  private static final Logger logger = LoggerFactory.getLogger(
    UserGrpcClientImpl.class
  );

  private final UserManagementServiceGrpc.UserManagementServiceBlockingStub userManagementStub;

  @Autowired
  public UserGrpcClientImpl(
    @Qualifier("userOrchestratorChannel") ManagedChannel userOrchestratorChannel
  ) {
    this.userManagementStub =
      UserManagementServiceGrpc.newBlockingStub(userOrchestratorChannel);
  }

  public User getUserByUserId(String userId) {
    try {
      UserOrchestrator.User grpcUser = userManagementStub.getUserByUserId(
        GetRequest.newBuilder().setId(userId).build()
      );
      return convertToModelUser(grpcUser);
    } catch (StatusRuntimeException e) {
      logger.error(
        "gRPC call for getUserByUserId failed with status: {}",
        e,
        e
      );
      throw new RuntimeException("gRPC failure for getUserByUserId: " );
    }
  }

  public User getUserByEmail(String email) {
    try {
      UserOrchestrator.User grpcUser = userManagementStub.getUserByEmail(
        GetRequest.newBuilder().setId(email).build()
      );
      return convertToModelUser(grpcUser);
    } catch (StatusRuntimeException e) {
      logger.error("gRPC call for getUserByEmail failed with status: {}");
      throw new RuntimeException("gRPC failure for getUserByEmail: " );
    }
  }

  public User createNewUser(User user) {
    try {
      CreateUserResponse response = userManagementStub.createNewUser(
        CreateUserRequest.newBuilder().setUser(convertToGrpcUser(user)).build()
      );
      return convertToModelUser(response.getUser());
    } catch (StatusRuntimeException e) {
      logger.error("gRPC call for createNewUser failed with status: {}");
      throw new RuntimeException("gRPC failure for createNewUser: " );
    }
  }

  public User updateUser(User user) {
    try {
      UpdateUserRequest.Builder requestBuilder = UpdateUserRequest
        .newBuilder()
        .setUserId(user.getUserId())
        .setUpdateMask(constructFieldMaskForUser(user))
        .setUser(convertToGrpcUser(user));

      UpdateUserResponse response = userManagementStub.updateUser(
        requestBuilder.build()
      );
      return convertToModelUser(response.getUser());
    } catch (StatusRuntimeException e) {
      logger.error("gRPC call for updateUser failed with status: {}");
      throw new RuntimeException("gRPC failure for updateUser: " );
    }
  }

  public void deleteUser(String userId) {
    try {
      userManagementStub.deleteUser(
        GetRequest.newBuilder().setId(userId).build()
      );
    } catch (StatusRuntimeException e) {
      logger.error("gRPC call for deleteUser failed with status: {}");
      throw new RuntimeException("gRPC failure for deleteUser: " );
    }
  }

  public MultipleUserResponseDTO getAllUsers(
    int start,
    int size,
    String filters,
    String sorting
  ) {
    // System.out.println(@Qualifier("userOrchestratorChannel"));
    try {
      GetAllUsersRequest request = GetAllUsersRequest
        .newBuilder()
        .setStart(start)
        .setSize(size)
        .setFilters(filters)
        .setSorting(sorting)
        .build();

      GetAllUsersResponse response = userManagementStub.getAllUsers(request);
      List<User> users = response
        .getUsersList()
        .stream()
        .map(this::convertToModelUser)
        .collect(Collectors.toList());
      int totalUsers = response.getTotalUsers();
      return new MultipleUserResponseDTO(users, totalUsers);
    } catch (StatusRuntimeException e) {
      logger.error("gRPC call for getAllUsers failed with status: {}");
      throw new RuntimeException("gRPC failure for getAllUsers: " );
    }
  }

  public boolean ping() {
    PingRequest request = PingRequest.newBuilder().build();
    PingResponse response = userManagementStub.ping(request);
    System.out.println(response.toString());
    boolean ret = false;
    if (response.getMessage().toString().equals("pong")) {
      ret = true;
    }
    return ret;
  }

  // Conversion and utility methods
  private User convertToModelUser(UserOrchestrator.User grpcUser) {
    List<GrantedAuthority> grantedAuthorities = grpcUser
      .getAuthoritiesList()
      .stream()
      .map(authority -> new SimpleGrantedAuthority(authority))
      .collect(Collectors.toList());

    User user = new User();
    user.setUserId(grpcUser.getUserId());
    user.setName(grpcUser.getFirstName() + " " + grpcUser.getLastName());
    user.setFirstName(grpcUser.getFirstName());
    user.setLastName(grpcUser.getLastName());
    user.setEmail(grpcUser.getEmail());
    user.setImageUrl(grpcUser.getImageUrl());
    user.setEmailVerified(grpcUser.getEmailVerified());
    user.setAuthorities(grantedAuthorities);
    user.setProvider(AuthProvider.valueOf(grpcUser.getProvider()));
    user.setEnabled(grpcUser.getIsEnabled());
    user.setCredentialsExpired(grpcUser.getIsCredentialsExpired());
    user.setExpired(grpcUser.getIsExpired());
    user.setLocked(grpcUser.getIsLocked());
    user.setRole(grpcUser.getRole());
    if (grpcUser.hasCreatedAt()) {
      Timestamp timestamp = grpcUser.getCreatedAt();
      Instant instant = Instant.ofEpochSecond(
        timestamp.getSeconds(),
        timestamp.getNanos()
      );
      user.setCreatedAt(instant);
    }
    return user;
  }

  private UserOrchestrator.User convertToGrpcUser(User user) {
    UserOrchestrator.User.Builder builder = UserOrchestrator.User.newBuilder();
    builder.setFirstName(user.getFirstName()); 
    builder.setLastName(user.getLastName()); 
    builder.setEmail(user.getEmail()); 
    
    builder.setEmailVerified(user.isEmailVerified());
    builder.setIsExpired(user.isExpired());
    builder.setIsLocked(user.isLocked());
    builder.setIsCredentialsExpired(user.isCredentialsExpired());
    builder.setIsEnabled(user.isEnabled());
    builder.setProvider(AuthProvider.valueOf(user.getProvider().toString()).toString());

    if (user.getImageUrl() != null) {
        builder.setImageUrl(user.getImageUrl());
    }

    if (user.getAuthorities() != null) {
        List<String> authorityStrings = user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());
        builder.addAllAuthorities(authorityStrings);
    }
    if (user.getRole() != null) {
        builder.setRole(user.getRole());
    }
    return builder.build();
}


  private FieldMask constructFieldMaskForUser(User user) {
    FieldMask.Builder maskBuilder = FieldMask.newBuilder();
    if (user.getUserId() != null) maskBuilder.addPaths("userid");
    if (user.getFirstName() != null) maskBuilder.addPaths("firstname");
    if (user.getLastName() != null) maskBuilder.addPaths("lastname");
    if (user.getEmail() != null) maskBuilder.addPaths("email");
    if (user.getImageUrl() != null) maskBuilder.addPaths("imageurl");
    if (user.isEmailVerified()) maskBuilder.addPaths("emailverified");
    if (user.getProvider() != null) maskBuilder.addPaths("provider");
    if (user.getAuthorities() != null) maskBuilder.addPaths("authorities");
    if (user.isExpired()) maskBuilder.addPaths("isexpired");
    if (user.isLocked()) maskBuilder.addPaths("islocked");
    if (user.isCredentialsExpired()) maskBuilder.addPaths(
      "iscredentialsexpired"
    );
    if (user.isEnabled()) maskBuilder.addPaths("isenabled");

    return maskBuilder.build();
  }
}
