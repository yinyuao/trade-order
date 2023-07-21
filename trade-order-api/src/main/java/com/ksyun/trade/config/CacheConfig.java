package com.ksyun.trade.config;

import com.ksyun.trade.cache.Cache;
import com.ksyun.trade.cache.MemoryCache;
import com.ksyun.trade.cache.RedisCache;
import com.ksyun.trade.cache.TwoLevelCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;

@Configuration
public class CacheConfig {

    @Autowired
    private JedisPool jedisPool;

    @Bean
    public Cache<String, String> twoLevelCache() {
        Cache<String, String> memoryCache = new MemoryCache<>(1000, 60 * 60 * 1000);
        Cache<String, String> redisCache = new RedisCache<>(jedisPool.getResource());
        return new TwoLevelCache<>(memoryCache, redisCache);
    }
}

