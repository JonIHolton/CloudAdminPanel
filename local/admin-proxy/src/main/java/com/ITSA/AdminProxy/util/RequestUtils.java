package com.ITSA.AdminProxy.util;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUtils {

    public static String extractBrowserInfo(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    public static String extractIpAddress(HttpServletRequest request) {
        // X-Forwarded-For header is used to get the original IP address of the client
        // when the request goes through a proxy or load balancer
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
