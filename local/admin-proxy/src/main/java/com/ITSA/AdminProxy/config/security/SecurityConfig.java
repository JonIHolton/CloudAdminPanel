package com.ITSA.AdminProxy.config.security;

import com.ITSA.AdminProxy.config.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.ITSA.AdminProxy.config.oauth2.handle.OAuth2AuthenticationFailureHandler;
import com.ITSA.AdminProxy.config.oauth2.handle.OAuth2AuthenticationSuccessHandler;
import com.ITSA.AdminProxy.config.oauth2.service.CustomOAuth2UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;


@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
  securedEnabled = true,
  jsr250Enabled = true,
  prePostEnabled = true
)
@RequiredArgsConstructor

public class SecurityConfig {

  private final CustomOAuth2UserService customOAuth2UserService;

  private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

  private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
  
  @Value("${app.defaultRedirectUrl}")
  private String defaultRedirectUrl;

  @Bean
  public TokenAuthenticationFilter tokenAuthenticationFilter() {
    return new TokenAuthenticationFilter();
  }

  /*
   * By default, Spring OAuth2 uses
   * HttpSessionOAuth2AuthorizationRequestRepository to save
   * the authorization request. But, since our service is stateless, we can't save
   * it in
   * the session. We'll save the request in a Base64 encoded cookie instead.
   */
  @Bean
  public HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository() {
    return new HttpCookieOAuth2AuthorizationRequestRepository();
  }

   @Bean
  public FilterRegistrationBean<CorsResponseFilter> corsResponseFilter() {
    FilterRegistrationBean<CorsResponseFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new CorsResponseFilter());
    registrationBean.addUrlPatterns("/*"); 
    return registrationBean;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(AbstractHttpConfigurer::disable)
      .cors(cors ->
        cors.configurationSource(CorsConfig.corsConfigurationSource())
      )
      .sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      .exceptionHandling(exception -> exception.authenticationEntryPoint(new CustomAuthenticationEntryPoint()))

      .oauth2Login(oauth ->
        oauth
          .authorizationEndpoint(endpoint ->
            endpoint
              .baseUri(SecurityConstants.BASE_OAUTH2_URI)
              .authorizationRequestRepository(
                cookieAuthorizationRequestRepository()
              )
          )
          .redirectionEndpoint(red ->
            red.baseUri(SecurityConstants.REDIRECT_URI_PATTERN)
          )
          .userInfoEndpoint(user -> user.userService(customOAuth2UserService))
          .successHandler(oAuth2AuthenticationSuccessHandler)
          .failureHandler(oAuth2AuthenticationFailureHandler)
      )
      .logout(logout -> logout
      .logoutUrl("/api/v1/auth/logout")
      .addLogoutHandler(new CustomLogoutHandler(null))
      .logoutSuccessHandler(new CustomLogoutSuccessHandler()))
      .authorizeHttpRequests(authorize ->
        authorize
          .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
          .requestMatchers(SecurityConstants.PUBLIC_URIS)
          .permitAll()
          .anyRequest()
          .authenticated()
      )
      .addFilterBefore(
        tokenAuthenticationFilter(),
      FilterSecurityInterceptor.class 
      );

    return http.build();
  }
}