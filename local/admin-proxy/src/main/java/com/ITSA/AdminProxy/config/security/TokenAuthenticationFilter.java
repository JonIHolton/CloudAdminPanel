package com.ITSA.AdminProxy.config.security;

import com.ITSA.AdminProxy.exception.CustomAuthenticationException;
import com.ITSA.AdminProxy.model.orchestrator.User;
import com.ITSA.AdminProxy.service.UserService;
import com.ITSA.AdminProxy.util.CookieUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

  @Autowired
  private TokenProvider tokenProvider;

  @Autowired
  private UserService customUserDetailsService;

  // @Autowired
  // private RedisTokenBlacklistService redisTokenBlacklistService;

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    String path = request.getRequestURI();
    if (path.startsWith("/actuator/health")) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      String jwt = getJwtFromRequest(request);

      if (
        jwt != null &&
        !jwt.isEmpty() &&
        // !redisTokenBlacklistService.isTokenBlacklisted(jwt) &&
        tokenProvider.validateToken(jwt)
      ) {
        String userId = tokenProvider.getUserIdFromToken(jwt);

        User userDetails = customUserDetailsService.loadUserById(userId);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          userDetails,
          null,
          userDetails.getAuthorities()
        );
        authentication.setDetails(
          new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
      } else {
        log.info("JWT is null, empty, or invalid.");
        throw new CustomAuthenticationException("Invalid Token");
      }
    } catch (CustomAuthenticationException ex) {
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      new ObjectMapper()
        .writeValue(
          response.getOutputStream(),
          Collections.singletonMap("error", ex.getMessage())
        );
      return; // Prevent further filter chain execution
    } catch (Exception ex) {
      log.error("Could not set user authentication in security context", ex);
      return;
    }

    filterChain.doFilter(request, response);
  }

  private String getJwtFromRequest(HttpServletRequest request) {
    Optional<jakarta.servlet.http.Cookie> cookie = CookieUtils.getCookie(
      request,
      SecurityConstants.AUTH_COOKIE_NAME
    );
    return cookie.map(jakarta.servlet.http.Cookie::getValue).orElse(null);
  }
}
