package com.ITSA.AdminProxy.grpc;

import com.ITSA.AdminProxy.model.orchestrator.Points;
import com.ITSA.AdminProxy.userorchestrator.UserManagementServiceGrpc;
import com.ITSA.AdminProxy.userorchestrator.UserOrchestrator;
import com.ITSA.AdminProxy.userorchestrator.UserOrchestrator.BanksResponse;
import com.ITSA.AdminProxy.userorchestrator.UserOrchestrator.GetRequest;
import com.ITSA.AdminProxy.userorchestrator.UserOrchestrator.PointsAccount;
import com.ITSA.AdminProxy.userorchestrator.UserOrchestrator.PointsRequest;
import com.ITSA.AdminProxy.userorchestrator.UserOrchestrator.PointsResponse;
import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class PointsGrpcClientImpl implements PointsGrpcClient {

  private static final Logger logger = LoggerFactory.getLogger(
    UserGrpcClientImpl.class
  );

  private final UserManagementServiceGrpc.UserManagementServiceBlockingStub userManagementStub;

  @Autowired
  public PointsGrpcClientImpl(
    @Qualifier("userOrchestratorChannel") ManagedChannel userOrchestratorChannel
  ) {
    this.userManagementStub =
      UserManagementServiceGrpc.newBlockingStub(userOrchestratorChannel);
  }

  @Override
  public List<Points> getPointsByUserId(String userId) {
    try {
      UserOrchestrator.PointsResponse grpcUser = userManagementStub.getUserPointsAccountsByUserId(
        GetRequest.newBuilder().setId(userId).build()
      );
      return convertToPointsModel(grpcUser);
    } catch (StatusRuntimeException e) {
      logger.info(
        "gRPC call for getUserByUserId failed with status: "
      );
      return new ArrayList<Points>();
  }
  }
  private List<Points> convertToPointsModel(
    UserOrchestrator.PointsResponse pointsResponse
  ) {
    List<Points> pointsList = new ArrayList<>();

    for (UserOrchestrator.PointsAccount pointsAccount : pointsResponse.getPointsAccountList()) {
      Points account = new Points();
      account.setBank(pointsAccount.getBank());
      account.setPoints(pointsAccount.getPoints());
      account.setPointsId(pointsAccount.getPointsid());
      account.setUserId(pointsAccount.getUserid());

      if (pointsAccount.hasCreatedAt()) {
        Timestamp timestamp = pointsAccount.getCreatedAt();
        Instant instant = Instant.ofEpochSecond(
          timestamp.getSeconds(),
          timestamp.getNanos()
        );
        account.setCreatedAt(instant);
      }
      if (pointsAccount.hasUpdatedAt()) {
        Timestamp timestamp = pointsAccount.getUpdatedAt();
        Instant instant = Instant.ofEpochSecond(
          timestamp.getSeconds(),
          timestamp.getNanos()
        );
        account.setUpdatedAt(instant);
      }

      pointsList.add(account);
    }

    return pointsList;
  }

  @Override
  public Points getPointsByPointsId(String pointsId) {
    try {
      PointsResponse response = userManagementStub.getPointsByPointsId(
        PointsRequest.newBuilder().setPointsid(pointsId).build()
      );
      return convertToPointsModel(response).stream().findFirst().orElse(null);
    } catch (StatusRuntimeException e) {
      logger.error(
        "gRPC call for getPointsByPointsId failed with status: {}"
      );
      throw new RuntimeException(
        "gRPC failure for getPointsByPointsId: " 
      );
    }
  }

  @Override
  public Points updatePoints(Points points) {
    try {
      PointsAccount pointsAccount = PointsAccount
        .newBuilder()
        .setUserid(points.getUserId())
        .setPointsid(points.getPointsId())
        .setBank(points.getBank())
        .setPoints(points.getPoints())
        // Assuming there's a method to convert Instant to Timestamp
        .setCreatedAt(
          Timestamps.fromMillis(points.getCreatedAt().toEpochMilli())
        )
        .setUpdatedAt(
          Timestamps.fromMillis(points.getUpdatedAt().toEpochMilli())
        )
        .build();

      PointsResponse response = userManagementStub.updatePoints(pointsAccount);
      return convertToPointsModel(response).stream().findFirst().orElse(null);
    } catch (StatusRuntimeException e) {
      logger.error("gRPC call for updatePoints failed with status: {}", e, e);
      throw new RuntimeException("gRPC failure for updatePoints: " + e, e);
    }
  }

  @Override
  public Points createNewAcount(Points newPointsAccount) {
    try {
      PointsAccount pointsAccount = PointsAccount
        .newBuilder()
        // Set fields from newPointsAccount
        .build();

      PointsResponse response = userManagementStub.createNewPointsAccount(
        pointsAccount
      );
      return convertToPointsModel(response).stream().findFirst().orElse(null);
    } catch (StatusRuntimeException e) {
      logger.error(
        "gRPC call for createNewPointsAccount failed with status: {}"
      );
      throw new RuntimeException(
        "gRPC failure for createNewPointsAccount: " 
      );
    }
  }

  @Override
  public List<Object> getAllBanks() {
    try {
      BanksResponse response = userManagementStub.getAllBanks(
        Empty.newBuilder().build()
      );
      return new ArrayList<>(response.getBanksList());
    } catch (StatusRuntimeException e) {
      logger.error("gRPC call for getAllBanks failed with status: {}", e, e);
      throw new RuntimeException("gRPC failure for getAllBanks: " + e, e);
    }
  }

  @Override
  public boolean deletePointsAccount(String pointsId) {
    try {
      // Prepare the request with the specified points ID
      PointsRequest request = PointsRequest.newBuilder().setPointsid(pointsId).build();
      // Execute the gRPC call to delete the points account
      userManagementStub.deletePointsAccount(request);
      // Return true to indicate successful deletion
      return true;
    } catch (StatusRuntimeException e) {
      logger.error("gRPC call for deletePointsAccount failed with status: {}", e.getStatus(), e);
      throw new RuntimeException("gRPC failure for deletePointsAccount: " + e, e);
    }
  }
  
}
