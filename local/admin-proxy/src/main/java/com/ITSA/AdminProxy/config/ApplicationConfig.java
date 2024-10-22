package com.ITSA.AdminProxy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.ITSA.AdminProxy.config.properties.CorsProperties;
import com.ITSA.AdminProxy.config.properties.OauthProperties;
import com.ITSA.AdminProxy.config.properties.RsaKeyProperties;
import com.ITSA.AdminProxy.config.properties.TokenProperties;
import com.ITSA.AdminProxy.fluentd.ConcreteLogObserver;
import com.ITSA.AdminProxy.fluentd.LogObserver;
import com.ITSA.AdminProxy.service.UserService;

import lombok.RequiredArgsConstructor;


@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(value = { RsaKeyProperties.class, TokenProperties.class, CorsProperties.class,
		OauthProperties.class })
public class ApplicationConfig {

	private final UserService userService;

	@Bean
	public UserDetailsService userDetailsService() {
		return userService;
	}

	@Bean
	public AuthenticationManager authenticationManager() {
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
		authenticationProvider.setUserDetailsService(userDetailsService());
		return new ProviderManager(authenticationProvider);
	}

	@Bean
public LogObserver logObserver() {
    return new ConcreteLogObserver();
}


}
