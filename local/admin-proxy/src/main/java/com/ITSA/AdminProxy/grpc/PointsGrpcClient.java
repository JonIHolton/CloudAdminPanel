package com.ITSA.AdminProxy.grpc;

import java.util.List;

import com.ITSA.AdminProxy.model.orchestrator.Points;

public interface PointsGrpcClient {
    
  List<Points> getPointsByUserId(String userId);

  Points getPointsByPointsId(String pointsId);

Points updatePoints(Points points);

Points createNewAcount(Points newPointsAccount);

List<Object> getAllBanks();

boolean deletePointsAccount(String pointsId);

}
