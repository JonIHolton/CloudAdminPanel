package com.ITSA.AdminProxy.config.security;

public interface SecurityConstants {
  String BASE_OAUTH2_URI = "/api/v1/oauth2/authorize";
  String REDIRECT_URI_PATTERN = "/oauth2/callback/*";
  String[] PUBLIC_URIS = new String[] {
    "/actuator/**",
    "/api/v1/auth/**",
    "/api/v1/oauth2/**",
    "/api/v1/login",
    "/error",
    "/favicon.ico",
    "/*/*.png",
    "/*/*.gif",
    "/*/*.svg",
    "/*/*.jpg",
    "/*/*.html",
    "/*/*.css",
    "/*/*.js",
  };
  String AUTH_COOKIE_NAME = "auth_token";
}
