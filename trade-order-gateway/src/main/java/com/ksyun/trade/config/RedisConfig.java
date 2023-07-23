package com.ksyun.trade.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisConfig {

    @Bean
    public JedisPool jedisPool(
            @Value("${jedis.redis.host}") String host,
            @Value("${jedis.redis.port}") Integer port,
            @Value("${jedis.redis.password:}") String password,
            @Value("${jedis.redis.db:0}") Integer db) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        return new JedisPool(poolConfig, host, port, 2000, password, db);
    }
}