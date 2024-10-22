package com.ITSA.AdminProxy.config.security;

// import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

// @Service
public class RedisTokenBlacklistService implements TokenBlacklistService {

    private final String redisTemplate;

    public RedisTokenBlacklistService(String redisTemplate) {
        this.redisTemplate = redisTemplate;
        // this.redisTemplate.setConnectionFactory(null);
    }

    @Override
    public void blacklistToken(String token) {
        // Use a prefix for keys related to blacklisted tokens for organization and easy retrieval
        String key = "blacklist:token:" + token;
        // redisTemplate.opsForValue().set(key, "blacklisted", 10, TimeUnit.MINUTES);
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        String key = "blacklist:token:" + token;
        // return redisTemplate.hasKey(key);
        return false;
    }
}
