package com.ITSA.AdminProxy.controller;

import com.ITSA.AdminProxy.annotation.CurrentUser;
import com.ITSA.AdminProxy.dto.*;
import com.ITSA.AdminProxy.exception.ResourceNotFoundException;
import com.ITSA.AdminProxy.fluentd.LogObserver;
import com.ITSA.AdminProxy.model.AuthProvider;
import com.ITSA.AdminProxy.model.orchestrator.Points;
import com.ITSA.AdminProxy.model.orchestrator.User;
import com.ITSA.AdminProxy.policy.Policy;
import com.ITSA.AdminProxy.service.AuthorizationService;
import com.ITSA.AdminProxy.service.PointsService;
import com.ITSA.AdminProxy.service.UserService;
import com.ITSA.AdminProxy.util.RequestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import com.ITSA.AdminProxy.service.LogServiceImpl;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

  private static final Logger logger = LoggerFactory.getLogger(
    UserController.class
  );

  @Autowired
  private UserService userService;

  @Autowired
  private PointsService pointsService;

  @Autowired
  private AuthorizationService authorizationService;

  @Autowired
  private Policy policy;

  @Autowired
  private LogObserver logObserver;

  @Autowired
  private LogServiceImpl LogServiceImpl;

  @Autowired
  private ObjectMapper objectMapper;


  @PostMapping("/api/v1/users/getAllUsers")
  public ResponseEntity<?> getAllUsers(
    @RequestBody ViewAllUserRequestDTO request,
    @CurrentUser User currentUser,
    HttpServletRequest httpRequest

  ) {
    String resource = "UserStorage";
    String actionRead = "read";

    String browserInfo = RequestUtils.extractBrowserInfo(httpRequest);
    String ipAddress = RequestUtils.extractIpAddress(httpRequest);

    if (!policy.hasPermission(currentUser, resource, actionRead)) {
      return new ResponseEntity<>(
        "You do not have permission to view all users.",
        HttpStatus.FORBIDDEN
      );
    }

    int start = request.getStart();
    int size = request.getSize();
    String sorting = request.getSorting() != null ? request.getSorting() : "";

    String filters = request.getFilters() != null ? request.getFilters() : "[]";
    Set<String> constraints = policy.getConstraintsForAction(
      currentUser,
      resource,
      actionRead
    );

    if (constraints.contains("exclude-admins")) {
      @SuppressWarnings("deprecation")
      JsonArray filtersJsonArray = new JsonParser()
        .parse(filters)
        .getAsJsonArray();

      JsonObject newFilter = new JsonObject();
      newFilter.addProperty("id", "role");
      newFilter.addProperty("value", "User"); 

      filtersJsonArray.add(newFilter);

      filters = filtersJsonArray.toString();
    }

    UserApiResponse usersResponse = userService.getAllUsers(
      start,
      size,
      filters,
      sorting
    );

   
    usersResponse
      .getMeta()
      .setTotalRowCount(usersResponse.getMeta().getTotalRowCount());

    return ResponseEntity.ok(usersResponse);
  }

  @GetMapping("/api/v1/users/{userid}")
  public ResponseEntity<?> getUserById(
    @PathVariable("userid") String userId,
    @CurrentUser User currentUser,
    HttpServletRequest httpRequest

  ) {
    if (!policy.hasPermission(currentUser, "UserStorage", "read")) {
      return new ResponseEntity<>(
        "You do not have permission to read user data.",
        HttpStatus.FORBIDDEN
      );
    }

    try {
      User userResponse = userService.findById(userId);
      if (userResponse == null) {
        return new ResponseEntity<>("User Not Found", HttpStatus.NOT_FOUND);
      }

      SingleUserResponseDTO response = SingleUserResponseDTO.convertToDTO(
        userResponse
      );
      return ResponseEntity.ok(response);
    } catch (ResourceNotFoundException e) {
      logger.error(e.getMessage());
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      logger.error("Error retrieving user by ID");
      return new ResponseEntity<>(
        "Failed to retrieve user",
        HttpStatus.INTERNAL_SERVER_ERROR
      );
    }
  }

  @GetMapping("/api/v1/users/{userid}/points/{pointsid}")
  public ResponseEntity<?> getPointsByPointsAndUserId(
    @PathVariable("userid") String userId,
    @PathVariable("pointsid") String pointsId,
    HttpServletRequest httpRequest
  ) {
    try {
      User userResponse = userService.findById(userId);
      if (userResponse == null) {
        throw new ResourceNotFoundException("User", "userId", userId);
      }

      Points userPointsAccounts = pointsService.findByPointsId(pointsId);
      if (!userPointsAccounts.getUserId().equals(userId)) {
        throw new Exception("Points Account does not belong to the requester");
      }

      return ResponseEntity.ok(userPointsAccounts);
    } catch (ResourceNotFoundException e) {
      logger.error(e.getMessage());
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      logger.error("User Account not found");
      return new ResponseEntity<>(
        "Failed to retrieve user",
        HttpStatus.INTERNAL_SERVER_ERROR
      );
    }
  }

  @PostMapping("/api/v1/users/{userid}/edit")
  public ResponseEntity<?> editUser(
    @PathVariable("userid") String userId,
    @RequestBody UserUpdateRequest updateRequest,
    @CurrentUser User currentUser,
    HttpServletRequest httpRequest

  ) {
    String resource = "UserStorage";
    String actionUpdate = "update";

    if (!policy.hasPermission(currentUser, resource, actionUpdate)) {
      return new ResponseEntity<>(
        "You do not have permission to update users.",
        HttpStatus.FORBIDDEN
      );
    }

    try {
      User user = userService.findById(userId);
      if (user == null) {
        throw new ResourceNotFoundException("User", "userId", userId);
      }

      if (
        currentUser.getUserId().equals(userId) &&
        updateRequest.getRole() != null
      ) {
        return new ResponseEntity<>(
          "You cannot edit your own role",
          HttpStatus.BAD_REQUEST
        );
      }

      String newRole = updateRequest.getRole();
      if (!authorizationService.getRoles().contains(user.getRole())) {
        return new ResponseEntity<>(
          "Incorrect Role Value",
          HttpStatus.BAD_REQUEST
        );
      }

      user.setName(updateRequest.getName());
      user.setFirstName(updateRequest.getFirstName());
      user.setLastName(updateRequest.getLastName());
      user.setEmail(updateRequest.getEmail());
      user.setRole(newRole);
      userService.updateUser(user);
      SingleUserResponseDTO returnUser = SingleUserResponseDTO.convertToDTO(
        user
      );
      String browserInfo = RequestUtils.extractBrowserInfo(httpRequest);
      String ipAddress = RequestUtils.extractIpAddress(httpRequest);
  
      logObserver.logAction(
        "User details updated. Initiator: " +
        currentUser.getUserId() +
        " (Role: " +
        currentUser.getRole() +
        "), Target User ID: " +
        userId +
        ", Action Time: " +
        System.currentTimeMillis() +
        " IP Address: "+ipAddress+" Browser Info: "+browserInfo,
        currentUser,
        userId
        ,browserInfo
        ,ipAddress
      );
      return ResponseEntity.ok(returnUser);
    } catch (ResourceNotFoundException e) {
      logger.error(e.getMessage());
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      logger.error("Error updating user");
      return new ResponseEntity<>(
        "Failed to update user",
        HttpStatus.INTERNAL_SERVER_ERROR
      );
    }
  }

  @GetMapping("/api/v1/users/{userid}/points")
  public ResponseEntity<?> getUserPointsByUserId(
    @PathVariable("userid") String userId,
    @CurrentUser User currentUser,
    HttpServletRequest httpRequest

  ) {
    String resource = "PointsLedger";
    String actionUpdate = "read";

    // Check for permission to update user
    if (!policy.hasPermission(currentUser, resource, actionUpdate)) {
      return new ResponseEntity<>(
        "You do not have permission to update users.",
        HttpStatus.FORBIDDEN
      );
    }

    try {
      User userResponse = userService.findById(userId);
      if (userResponse == null) {
        throw new ResourceNotFoundException("User", "userId", userId);
      }
      List<Points> userPointsAccounts = pointsService.findByUserId(userId);
      UserAndPointResponse returnUser = UserAndPointResponse.convertToDTO(
        userResponse,
        userPointsAccounts
      );
      UserAndPointsApiResponse response = new UserAndPointsApiResponse(
        returnUser,
        returnUser.getPointAccount().size()
      );
      return ResponseEntity.ok(response);
    } catch (ResourceNotFoundException e) {
      logger.error(e.getMessage());
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      logger.error("Error retrieving user by ID");
      return new ResponseEntity<>("No Points found", HttpStatus.NOT_FOUND);
    }
  }

  @PostMapping("/api/v1/users/{userid}/points/{pointsid}/edit")
  public ResponseEntity<?> editPointsAccount(
    @PathVariable("userid") String userId,
    @PathVariable("pointsid") String pointsId,
    @RequestBody PointsUpdateRequest updateRequest,
    @CurrentUser User currentUser,
    HttpServletRequest httpRequest

  ) {
    String resource = "PointsLedger";
    String actionUpdate = "update";
    if (!policy.hasPermission(currentUser, resource, actionUpdate)) {
      return new ResponseEntity<>(
        "You do not have permission to update points.",
        HttpStatus.FORBIDDEN
      );
    }

    try {
      Points pointsAccount = pointsService.findByPointsId(pointsId);
      if (pointsAccount == null || !pointsAccount.getUserId().equals(userId)) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }

      pointsAccount.setPoints(updateRequest.getPoints());
      pointsService.updatePoints(pointsAccount);
      String browserInfo = RequestUtils.extractBrowserInfo(httpRequest);
      String ipAddress = RequestUtils.extractIpAddress(httpRequest);
  
      logObserver.logAction(
        "Points account edited. Initiator: " +
        currentUser.getUserId() +
        ", Target User ID: " +
        userId +
        ", Points Account ID: " +
        pointsId +
        ", Points Updated To: " +
        updateRequest.getPoints() +
        ", Action Time: " +
        System.currentTimeMillis() +
        " IP Address: "+ipAddress+" Browser Info: "+browserInfo,       
        currentUser,
        userId,
        browserInfo,
        ipAddress
      );
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      logger.error("Error updating points account");
      return new ResponseEntity<>(
        "Failed to update points account",
        HttpStatus.INTERNAL_SERVER_ERROR
      );
    }
  }

  @DeleteMapping("/api/v1/users/{userid}/points/{pointsid}")
  public ResponseEntity<?> deletePointsAccount(
    @PathVariable("userid") String userId,
    @PathVariable("pointsid") String pointsId,
    @CurrentUser User currentUser,
    HttpServletRequest httpRequest

  ) {
    String resource = "PointsLedger";
    String actionDelete = "delete";
    if (!policy.hasPermission(currentUser, resource, actionDelete)) {
      return new ResponseEntity<>(
        "You do not have permission to delete points accounts.",
        HttpStatus.FORBIDDEN
      );
    }

    try {
      Points pointsAccount = pointsService.findByPointsId(pointsId);
      if (pointsAccount == null || !pointsAccount.getUserId().equals(userId)) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }

      pointsService.deletePoints(pointsId);
      String browserInfo = RequestUtils.extractBrowserInfo(httpRequest);
      String ipAddress = RequestUtils.extractIpAddress(httpRequest);
  
      logObserver.logAction(
        "Points account deleted. Initiator: " +
        currentUser.getUserId() +
        ", Target User ID: " +
        userId +
        ", Points Account ID: " +
        pointsId +
        ", Action Time: " +
        System.currentTimeMillis() +
        " IP Address: "+ipAddress+" Browser Info: "+browserInfo,
        currentUser,
        userId,
        browserInfo,
        ipAddress
      );
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      logger.error("Error deleting points account");
      return new ResponseEntity<>(
        "Failed to delete points account",
        HttpStatus.INTERNAL_SERVER_ERROR
      );
    }
  }

  
  @GetMapping("/api/v1/getAllBanks")
  public ResponseEntity<?> getAllBanks(    HttpServletRequest httpRequest
  ) {
    try {
      List<Object> banks = pointsService.getAllBanks();
      return ResponseEntity.ok(banks);
    } catch (Exception e) {
      logger.error("Failed to retrieve banks");
      return new ResponseEntity<>(
        "Failed to retrieve banks",
        HttpStatus.INTERNAL_SERVER_ERROR
      );
    }
  }

  @GetMapping("/api/v1/user/me")
  public ResponseEntity<?> getCurrentUser(@CurrentUser User userPrincipal,
  HttpServletRequest httpRequest
  ) {
    try {
      User user = userService.findById(userPrincipal.getUserId());
      if (user == null) {
        throw new ResourceNotFoundException(
          "User",
          "userId",
          userPrincipal.getUserId()
        );
      }
      UserDTO dto = UserDTO.convertToDTO(user);
      return ResponseEntity.ok(dto);
    } catch (ResourceNotFoundException e) {
      logger.error(e.getMessage());
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      logger.error("Error retrieving current user");
      return new ResponseEntity<>(
        "Failed to retrieve current user",
        HttpStatus.INTERNAL_SERVER_ERROR
      );
    }
  }

  @PostMapping("/api/v1/users/createNewUser")
  public ResponseEntity<?> createNewUser(
    @RequestBody CreateNewUserRequest newUser,
    @CurrentUser User currentUser,
    HttpServletRequest httpRequest

  ) {
    String resource = "UserStorage";
    String actionDelete = "create";

    if (!policy.hasPermission(currentUser, resource, actionDelete)) {
      return new ResponseEntity<>(
        "You do not have permission to delete users.",
        HttpStatus.FORBIDDEN
      );
    }
    try {
      if (
        newUser.getFirstName() == null ||
        newUser.getLastName() == null ||
        newUser.getEmail() == null ||
        newUser.getFirstName().trim().isEmpty() ||
        newUser.getLastName().trim().isEmpty() ||
        newUser.getEmail().trim().isEmpty()
      ) {
        throw new IllegalArgumentException(
          "First name, last name, and email are required"
        );
      }

      List<String> validRoles = authorizationService.getRoles();

      if (
        !(newUser.getRole().isEmpty() || validRoles.contains(newUser.getRole()))
      ) {
        throw new IllegalArgumentException("Invalid role provided");
      }

      String email = newUser.getEmail().trim();
      if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
        throw new IllegalArgumentException("Invalid email address");
      }

      if (newUser.getFirstName().trim().length() < 3) {
        throw new IllegalArgumentException(
          "First name must be at least 3 characters long"
        );
      }

      User toCreate = new User();
      toCreate.setEmail(newUser.getEmail());
      toCreate.setFirstName(newUser.getFirstName());
      toCreate.setLastName(newUser.getLastName());
      toCreate.setName(newUser.getFirstName() + " " + newUser.getLastName());
      toCreate.setProvider(AuthProvider.google);
      toCreate.setRole(newUser.getRole());

      User createdUser = userService.registerNewUser(toCreate);
      SingleUserResponseDTO response = SingleUserResponseDTO.convertToDTO(
        createdUser
      );
      String browserInfo = RequestUtils.extractBrowserInfo(httpRequest);
      String ipAddress = RequestUtils.extractIpAddress(httpRequest);
  
      logObserver.logAction(
        "New user created. Initiator: " +
        currentUser.getUserId() +
        " (Role: " +
        currentUser.getRole() +
        "), New User ID: " +
        createdUser.getUserId() +
        ", Action Time: " +
        System.currentTimeMillis() +
        " IP Address: "+ipAddress+" Browser Info: "+browserInfo,
        currentUser,
        createdUser.getUserId(),
        browserInfo,
        ipAddress
      );
      return new ResponseEntity<>(response, HttpStatus.CREATED);
    } catch (IllegalArgumentException e) {
      logger.error(e.getMessage());
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      logger.error("Failed to create new user");
      return new ResponseEntity<>(
        "Failed to create new user",
        HttpStatus.INTERNAL_SERVER_ERROR
      );
    }
  }

  @DeleteMapping("/api/v1/users/{userid}")
  public ResponseEntity<?> deleteUser(
    @PathVariable("userid") String userId,
    @CurrentUser User currentUser,
    HttpServletRequest httpRequest

  ) {
    String resource = "UserStorage";
    String actionDelete = "delete";
    if (currentUser.getUserId().equals(userId)) {
      return new ResponseEntity<>(
        "You cannot delete your own account",
        HttpStatus.BAD_REQUEST
      );
    }

    // Check for permission to delete user
    if (!policy.hasPermission(currentUser, resource, actionDelete)) {
      return new ResponseEntity<>(
        "You do not have permission to delete users.",
        HttpStatus.FORBIDDEN
      );
    }
    try {
      User user = userService.findById(userId);
      if (user == null) {
        return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
      }
      List<Points> userPointsAccounts = pointsService.findByUserId(userId);

      // Attempt to delete each Points account and collect the results
      boolean allDeleted = true;
      for (Points points : userPointsAccounts) {
        boolean deleted = pointsService.deletePoints(points.getPointsId());
        if (!deleted) {
          allDeleted = false;
          logger.error(
            "Failed to delete Points account with ID: {}",
            points.getPointsId()
          );
          break; // Exit the loop if any deletion fails
        }
      }

      // Only proceed with user deletion if all Points accounts were successfully deleted
      if (allDeleted) {
        userService.deleteUser(userId);
        String browserInfo = RequestUtils.extractBrowserInfo(httpRequest);
        String ipAddress = RequestUtils.extractIpAddress(httpRequest);
    
        logObserver.logAction(
          "User and associated points accounts deleted. Initiator: " +
          currentUser.getUserId() +
          ", Target User ID: " +
          userId +
          ", Action Time: " +
          System.currentTimeMillis() +
          " IP Address: "+ipAddress+" Browser Info: "+browserInfo,          
          currentUser,
          userId,
          browserInfo,
          ipAddress
        );
        return new ResponseEntity<>(
          "User and associated points accounts deleted successfully",
          HttpStatus.OK
        );
      } else {
        return new ResponseEntity<>(
          "Failed to delete one or more Points accounts, user deletion aborted",
          HttpStatus.CONFLICT 
        );
      }
    } catch (Exception e) {
      logger.error("Failed to delete user");
      return new ResponseEntity<>(
        "Failed to delete user",
        HttpStatus.INTERNAL_SERVER_ERROR
      );
    }
  }

  @GetMapping("/api/v1/roles")
  public ResponseEntity<?> getRoles() {
    try {
      List<String> roles = authorizationService.getRoles();
      return ResponseEntity.ok(roles);
    } catch (Exception e) {
      logger.error("Failed to retrieve roles");
      return new ResponseEntity<>(
        "Failed to retrieve roles",
        HttpStatus.INTERNAL_SERVER_ERROR
      );
    }
  }

  @GetMapping("/api/v1/policy")
  public ResponseEntity<?> getPolicy() {
    try {
      Map<String, Object> policy = authorizationService.getPolicyDocument();
      return ResponseEntity.ok(policy);
    } catch (Exception e) {
      logger.error("Failed to retrieve policy");
      return new ResponseEntity<>(
        "Failed to retrieve policy",
        HttpStatus.INTERNAL_SERVER_ERROR
      );
    }
  }
  
  @PostMapping("/api/v1/logs/query")
  public ResponseEntity<?> getLogs(
    @RequestBody TempLogRequestDTO request,
    @CurrentUser User currentUser
  ) {
    String resource = "Logs";
    String actionDelete = "read";

    if (!policy.hasPermission(currentUser, resource, actionDelete)) {
      return new ResponseEntity<>(
        "You do not have permission to delete points accounts.",
        HttpStatus.FORBIDDEN
      );
    }
    // c.I.A.controller.UserController : 
    // Receiving requests: ViewLogRequestDTO(logId=null, startTimestamp=0.0, 
    // endTimestamp=0.0, searchTimestamp=1.912425714E9, description=null, initiatorUser=null, targetUser=null, sort=null, page=0, size=10)
      logger.info("Receiving requests: " + request.toString());


      // create temporary object
      ViewLogRequestDTO tempRequest = new ViewLogRequestDTO();
      tempRequest.setStart(request.getStart());
      tempRequest.setSize(request.getSize());
      List<TempLogRequestDTO.Filter> filterList;
      try {
        filterList = objectMapper.readValue(request.getFilters(), new TypeReference<List<TempLogRequestDTO.Filter>>(){});
      } catch (JsonMappingException e) {
        filterList = new ArrayList<>();
        e.printStackTrace();
    
      } catch (JsonProcessingException e) {
        filterList = new ArrayList<>();
        e.printStackTrace();
      }



      for (TempLogRequestDTO.Filter filter : filterList) {
        switch (filter.getId()) {
            case "logId":
                tempRequest.setLogId((String) filter.getValue());
                break;
            case "description":
                tempRequest.setDescription((String) filter.getValue());
                break;
            case "userId":
                tempRequest.setInitiatorUser((String) filter.getValue());
                tempRequest.setTargetUser((String) filter.getValue());
                break;
          }
        }
  


      ResponseEntity<?> responseEntity = LogServiceImpl.getLogs(tempRequest);

      logger.error("Response from logging service: " + responseEntity.toString());
      return ResponseEntity.status(responseEntity.getStatusCode()).body(responseEntity.getBody());
        
    }



    
    @GetMapping("/api/v1/logs/{logsId}")
    public ResponseEntity<?> getLogsById(
      @PathVariable("logsId") String logsId,

      @CurrentUser User currentUser
    ) {
      String resource = "Logs";
      String actionDelete = "read";
  
      // Check for permission to delete points
      if (!policy.hasPermission(currentUser, resource, actionDelete)) {
        return new ResponseEntity<>(
          "You do not have permission to delete points accounts.",
          HttpStatus.FORBIDDEN
        );
      }
        logger.info("Receiving requests: " + logsId.toString());

        if (logsId == null || logsId.isEmpty()) {
          return new ResponseEntity<>(
            "Invalid log ID provided",
            HttpStatus.BAD_REQUEST
          );
        }
        
        return LogServiceImpl.getLogById(logsId);

      }

}
