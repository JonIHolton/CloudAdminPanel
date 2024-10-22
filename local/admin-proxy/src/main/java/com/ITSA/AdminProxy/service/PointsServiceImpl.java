package com.ITSA.AdminProxy.service;

import com.ITSA.AdminProxy.grpc.PointsGrpcClient;
import com.ITSA.AdminProxy.model.orchestrator.Points;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PointsServiceImpl implements PointsService {

  private static final Logger logger = LoggerFactory.getLogger(
    UserServiceImpl.class
  );

  private final PointsGrpcClient pointsGrpcClient;

  @Autowired
  public PointsServiceImpl(PointsGrpcClient pointsGrpcClient) {
    this.pointsGrpcClient = pointsGrpcClient;
  }

  @Override
  public List<Points> findByUserId(String userId) {
    try {
      List<Points> response = pointsGrpcClient.getPointsByUserId(userId);

      return response;
    } catch (Exception e) {
      logger.error("Error occurred while registering new user", e);
      throw e; // Custom exception handling as per your application's needs
    }
  }

  @Override
  public Points findByPointsId(String pointsId) {
    try {
      Points response = pointsGrpcClient.getPointsByPointsId(pointsId);

      return response;
    } catch (Exception e) {
      logger.error("Error occurred while registering new user", e);
      throw e; // Custom exception handling as per your application's needs
    }
  }

  @Override
  public Points updatePoints(Points points) {
    try {
      Points response = pointsGrpcClient.updatePoints(points);

      return response;
    } catch (Exception e) {
      logger.error("Error occurred while registering new user", e);
      throw e; // Custom exception handling as per your application's needs
    }
  }

  @Override
  public boolean deletePoints(String PointsAccount) {
    try {
      boolean deleted = false;

      if(pointsGrpcClient.deletePointsAccount(PointsAccount)) {
        deleted = true;
      }

      return deleted;
    } catch (Exception e) {
      logger.error("Error occurred while registering new user", e);
      throw e; // Custom exception handling as per your application's needs
    }
  }

  @Override
  public void createNewAccount(Points newPointsAccount) {
    try {
      Points response = pointsGrpcClient.createNewAcount(newPointsAccount);
    } catch (Exception e) {
      logger.error("Error occurred while registering new user", e);
      throw e; // Custom exception handling as per your application's needs
    }
  }

  @Override
  public List<Object> getAllBanks() {
    try {
      List<Object> response = pointsGrpcClient.getAllBanks();

      return response;
    } catch (Exception e) {
      logger.error("Error occurred while registering new user", e);
      throw e; // Custom exception handling as per your application's needs
    }
  }
}
