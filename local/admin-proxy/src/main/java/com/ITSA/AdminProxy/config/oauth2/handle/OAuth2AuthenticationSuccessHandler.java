package com.ITSA.AdminProxy.config.oauth2.handle;

import static com.ITSA.AdminProxy.config.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

import com.ITSA.AdminProxy.config.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.ITSA.AdminProxy.config.properties.OauthProperties;
import com.ITSA.AdminProxy.config.security.SecurityConstants;
import com.ITSA.AdminProxy.config.security.TokenProvider;
import com.ITSA.AdminProxy.exception.BadRequestException;
import com.ITSA.AdminProxy.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler
  extends SimpleUrlAuthenticationSuccessHandler {

  private final TokenProvider tokenProvider;

  private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

  private final OauthProperties oauthProperties;



  @Override
  public void onAuthenticationSuccess(
    HttpServletRequest request,
    HttpServletResponse response,
    Authentication authentication
  ) throws IOException {
    try {
      String targetUrl = determineTargetUrl(request, response, authentication);

      if (response.isCommitted()) {
        log.info(
          "Response has already been committed. Unable to redirect to {}",
          targetUrl
        );
        return;
      }

      clearAuthenticationAttributes(request, response);
      log.info("Redirecting to {}", targetUrl);
      getRedirectStrategy().sendRedirect(request, response, targetUrl);
    } catch (Exception ex) {
      log.error("Error handling OAuth2 authentication success", ex);
      throw new BadRequestException("Failed to process authentication success");
    }
  }

  @Override
  protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
      Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME).map(Cookie::getValue);
  
      if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
          log.error("Unauthorized Redirect URI: {}", redirectUri.get());
          throw new BadRequestException("Unauthorized Redirect URI");
      }
  
      String targetUrl = redirectUri.orElse(getDefaultTargetUrl());
  
      String token = null;
      try {
          token = tokenProvider.createToken(authentication);
          CookieUtils.addCookie(response, SecurityConstants.AUTH_COOKIE_NAME, token, 24 * 60 * 60); 
      } catch (MalformedURLException e) {
          log.error(e.toString());
      }
  
      return targetUrl;
  }
  protected void clearAuthenticationAttributes(
    HttpServletRequest request,
    HttpServletResponse response
  ) {
    super.clearAuthenticationAttributes(request);
    httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(
      request,
      response
    );
  }

  private boolean isAuthorizedRedirectUri(String uri) {
    URI clientRedirectUri = URI.create(uri);

    return List
      .of(oauthProperties.getAuthorizedRedirectUris())
      .stream()
      .anyMatch(authorizedRedirectUri -> {
        // Only validate host and port. Let the clients use different paths if they want
        // to
        URI authorizedURI = URI.create(authorizedRedirectUri);
        return (
          authorizedURI
            .getHost()
            .equalsIgnoreCase(clientRedirectUri.getHost()) &&
          authorizedURI.getPort() == clientRedirectUri.getPort()
        );
      });
  }
}
