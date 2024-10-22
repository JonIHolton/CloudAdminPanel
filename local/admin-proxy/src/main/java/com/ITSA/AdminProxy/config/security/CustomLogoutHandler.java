package com.ITSA.AdminProxy.config.security;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import com.ITSA.AdminProxy.util.CookieUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;

public class CustomLogoutHandler implements LogoutHandler {

    private TokenBlacklistService tokenBlacklistService;

    public CustomLogoutHandler(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }


    private String extractToken(HttpServletRequest request) {
       Optional<Cookie> cookie =  CookieUtils.getCookie(request, SecurityConstants.AUTH_COOKIE_NAME);
       // null check
         if (cookie.isPresent()) {
            return cookie.get().getValue();
         }
        
        return null;
    }

    @Override
    public void logout(jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response, Authentication authentication) {
                String token = extractToken(request);
                // if (token != null && !token.isEmpty()) {
                //     tokenBlacklistService.blacklistToken(token);
                // }
        
                // Delete the OAuth2 authorization request cookie
                CookieUtils.deleteCookie(request, response, SecurityConstants.AUTH_COOKIE_NAME);   
                SecurityContextHolder.clearContext();

             }
}
