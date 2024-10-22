package com.ITSA.AdminProxy.fluentd;

import com.ITSA.AdminProxy.model.orchestrator.User;

public interface LogObserver {
    void logAction(String actionDescription, User initiator, String targetUserId, String browserInformation, String ipAddress);
}
