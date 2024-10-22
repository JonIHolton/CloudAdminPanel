package com.ITSA.AdminProxy.service;

import java.util.List;
import java.util.Map;

public interface AuthorizationService {

    boolean checkPermission(String roleName, String resource, String permission);

    Map<String, Object> getPolicyDocument();

    List<String> getRoles();

}