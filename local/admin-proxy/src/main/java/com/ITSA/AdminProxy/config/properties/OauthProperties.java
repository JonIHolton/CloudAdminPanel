package com.ITSA.AdminProxy.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "oauth")
@Getter
@Setter
public class OauthProperties {

    private String[] authorizedRedirectUris;


}
