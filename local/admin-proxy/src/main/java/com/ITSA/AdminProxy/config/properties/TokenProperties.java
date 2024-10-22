package com.ITSA.AdminProxy.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "token")
public class TokenProperties {

    private Integer expiresMinutes; 

    private String domain;

}
