package com.ITSA.AdminProxy.config.security;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import com.ITSA.AdminProxy.config.properties.TokenProperties;
import com.ITSA.AdminProxy.model.orchestrator.User;
import com.ITSA.AdminProxy.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenProvider {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final TokenProperties tokenProperties;
    @Value("${spring.security.jwt.issuer}")
    private String jwtIssuer;


    @Value("${token.expiresMinutes}")
    private int jwtExpiresMinutes;

    public String createToken(Authentication authentication) throws MalformedURLException {
        User userPrincipal = (User) authentication.getPrincipal();
        Instant now = Instant.now();
        URL issuerUrl = new URL(jwtIssuer);


        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
        .issuer(issuerUrl.toExternalForm())
        .subject(userPrincipal.getEmail())
        .issuedAt(now)
        .expiresAt(now.plus(jwtExpiresMinutes, ChronoUnit.MINUTES))
        .claim(JwtUtils.USER_ID.getProperty(), userPrincipal.getUserId())
        .claim(JwtUtils.NAME.getProperty(), userPrincipal.getFirstName() +" "+ userPrincipal.getLastName())
        .claim(JwtUtils.SCOPE.getProperty(), authentication.getAuthorities())
        .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet)).getTokenValue();
    }

    public boolean validateToken(String token) throws MalformedURLException {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            
            validateClaims(jwt);

            return true;
        } catch (Exception e) {
            log.error("Token validation failed for token : " +token.toString());
            return false;
        }
    }

    private void validateClaims(Jwt jwt) throws JwtValidationException, MalformedURLException {
        // Issuer validation
        URL issuerUrl = new URL(jwtIssuer);
        if (!issuerUrl.equals(jwt.getIssuer())) {
            throw new JwtValidationException("Invalid issuer.", null);
        }

        // Subject validation
        if (jwt.getSubject() == null) {
            throw new JwtValidationException("Invalid subject.", null);
        }

        // // Audience validation
        // List<String> expectedAudience = List.of("YourAudience"); // Replace with your expected audience
        // if (jwt.getAudience() == null || Collections.disjoint(jwt.getAudience(), expectedAudience)) {
        //     throw new JwtValidationException("Invalid audience.", null);
        // }

        // Expiration time validation
        if (jwt.getExpiresAt() != null && jwt.getExpiresAt().isBefore(Instant.now() )) {
            throw new JwtValidationException("JWT is expired.", null);
        }

        // Not before time validation
        if (jwt.getNotBefore() != null && jwt.getNotBefore().isAfter(Instant.now())) {
            throw new JwtValidationException("JWT not valid yet.", null);
        }

        // Issued at time validation
        if (jwt.getIssuedAt() != null && jwt.getIssuedAt().isBefore(Instant.now().minusSeconds(tokenProperties.getExpiresMinutes() * 60))) {
            throw new JwtValidationException("JWT issued at time is invalid.", null);
        }

    }
    public String getUserIdFromToken(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        return jwt.getClaim(JwtUtils.USER_ID.getProperty());
    }

}
