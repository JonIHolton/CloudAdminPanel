package com.ITSA.AdminProxy.policy;

import com.ITSA.AdminProxy.model.orchestrator.User;
import com.ITSA.AdminProxy.service.AuthorizationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Component
public class Policy {

    private final Map<String, JsonNode> rolePolicies = new HashMap<>();
    private final AuthorizationService authorizationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public Policy(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
        loadPolicies();
    }

    private void loadPolicies() {
        try {
            Map<String, Object> resource = authorizationService.getPolicyDocument();
            resource.forEach((role, value) -> {
                try {
                    JsonNode permissions = objectMapper.valueToTree(value);
                    rolePolicies.put(role, permissions);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to convert policy data for role: " + role, e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to load policy file", e);
        }
    }

    public boolean hasPermission(User user, String resource, String action) {
        JsonNode permissionsForRole = rolePolicies.get(user.getRole());
        if (permissionsForRole == null) {
            return false;
        }
        JsonNode permissionsForResource = permissionsForRole.get(resource);
        if (permissionsForResource == null) {
            return false;
        }
        JsonNode actions = permissionsForResource.get("actions");
        if (actions.isArray()) {
            for (JsonNode actionNode : actions) {
                if (actionNode.asText().equals(action)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Set<String> getConstraintsForAction(User user, String resource, String action) {
        Set<String> constraints = new HashSet<>();
    
        JsonNode permissionsForRole = rolePolicies.get(user.getRole());
        if (permissionsForRole == null) {
            return constraints;
        }
    
        JsonNode permissionsForResource = permissionsForRole.get(resource);
        if (permissionsForResource == null) {
            return constraints; 
        }
    
        JsonNode actionsNode = permissionsForResource.get("actions");
        if (actionsNode != null && actionsNode.isArray()) {
            for (JsonNode actionNode : actionsNode) {
                if (actionNode.asText().equals(action)) {
                    JsonNode constraintsNode = permissionsForResource.get("constraints");
                    if (constraintsNode != null && constraintsNode.isArray()) {
                        for (JsonNode constraintNode : constraintsNode) {
                            constraints.add(constraintNode.asText()); 
                        }
                    }
                    break; 
                }
            }
        }
    
        return constraints;
    }
    
  
}
