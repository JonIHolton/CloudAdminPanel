package com.ITSA.transaction.config;

import redis.clients.jedis.Jedis;

public class CacheConfig {
    private static final String CACHE_HOST = "redis";
    private static final int CACHE_PORT = 6379;
    private static final String CACHE_PASSWORD = "password"; 

    public static Jedis getJedisConnection() {
        Jedis jedis = new Jedis(CACHE_HOST, CACHE_PORT);
        jedis.auth(CACHE_PASSWORD); 
        return jedis;
    }
}
