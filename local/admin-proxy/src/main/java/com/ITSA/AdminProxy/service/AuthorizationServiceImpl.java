package com.ITSA.AdminProxy.service;

import com.ITSA.AdminProxy.util.HttpClientUtility;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {

  private static final Logger logger = LoggerFactory.getLogger(
    AuthorizationServiceImpl.class
  );

  @Autowired
  private HttpClientUtility httpClientUtility;

  private static final String POLICY_URL = "http://127.0.0.1:8002/policies";
  private static final String ROLES_URL = "http://127.0.0.1:8002/getroles";
  // private static final int MAX_RETRY = 3; // Maximum number of retries
  // private static final String POLICY_URL = "http://authorisation:8002/policies";
  // private static final String ROLES_URL = "http://authorisation:8002/getroles";
  private static final int MAX_RETRY = 3; // Maximum number of retries

  private static final long RETRY_DELAY_MS = 5000; // Delay between retries in milliseconds

   // Cache storage
    private Map<String, Object> policyCache = new ConcurrentHashMap<>();
    private List<String> rolesCache = Collections.emptyList();

    // Cache update flags
    private volatile boolean isPolicyCacheValid = false;
    private volatile boolean isRolesCacheValid = false;

    // Scheduled task to refresh cache
    @Scheduled(fixedRate = 60000) // Refresh every 60 seconds
    public void refreshCache() {
        isPolicyCacheValid = false;
        isRolesCacheValid = false;
    }
  @Override
  public boolean checkPermission(
    String roleName,
    String resource,
    String permission
  ) {
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> policy = httpClientUtility.getForObject(
        POLICY_URL,
        Map.class
      );
      // Add logic to interpret the policy document and check permissions
      // This is a simplified example; actual implementation will depend on the policy structure
      Map<String, Object> roles = (Map<String, Object>) policy.get("roles");
      if (roles != null) {
        Map<String, Object> rolePermissions = (Map<String, Object>) roles.get(
          roleName
        );
        if (rolePermissions != null) {
          Map<String, Boolean> resourcePermissions = (Map<String, Boolean>) rolePermissions.get(
            resource
          );
          return Boolean.TRUE.equals(resourcePermissions.get(permission));
        }
      }
    } catch (Exception e) {
      // Handle error (e.g., policy service down, invalid response)
    }
    return false;
  }

  @Override
  public List<String> getRoles() {
      if (!isRolesCacheValid) {
          synchronized (this) {
              // Double-check locking
              if (!isRolesCacheValid) {
                  try {
                      List<String> roles = httpClientUtility.getListOfString(ROLES_URL);
                      if (roles != null && !roles.isEmpty()) {
                          rolesCache = roles;
                      } else {
                          logger.error("The roles list returned was empty.");
                      }
                  } catch (Exception e) {
                      logger.error("Failed to retrieve roles from the Flask service", e);
                  }
                  isRolesCacheValid = true;
              }
          }
      }
      return rolesCache;
  }
  
  @Override
  public Map<String, Object> getPolicyDocument() {
    if (!isPolicyCacheValid) {
        synchronized (this) {
            if (!isPolicyCacheValid) {
                int attempt = 0;
                while (attempt < MAX_RETRY) {
                    try {
                        Map<String, Object> policy = httpClientUtility.getForObject(POLICY_URL, Map.class);
                        policyCache = policy;
                        isPolicyCacheValid = true;
                        return policyCache;
                    } catch (Exception e) {
                        logger.error("Failed to retrieve policy document from the service on attempt " + (attempt + 1), e);
                        attempt++;
                        if (attempt < MAX_RETRY) {
                            try {
                                Thread.sleep(RETRY_DELAY_MS); // Wait before retrying
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt(); // Re-interrupt the thread
                                logger.error("Retry interrupted", ie);
                                return Collections.emptyMap(); // or handle differently
                            }
                        }
                    }
                }
                logger.error("Failed to retrieve policy document after " + MAX_RETRY + " attempts");
            }
        }
    }
    return policyCache;
}
  
}
