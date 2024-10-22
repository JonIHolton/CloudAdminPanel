package com.ITSA.AdminProxy.service;


import java.util.List;

import com.ITSA.AdminProxy.model.orchestrator.Points;

public interface PointsService{

List<Points> findByUserId(String userId);
Points findByPointsId(String pointsId);

Points updatePoints(Points points);
boolean deletePoints(String PointsAccount);
void createNewAccount(Points newPointsAccount);
List<Object> getAllBanks();


}