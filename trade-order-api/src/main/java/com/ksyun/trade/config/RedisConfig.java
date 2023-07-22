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
            @Value("${spring.redis.host}") String host,
            @Value("${spring.redis.port}") Integer port,
            @Value("${spring.redis.password:}") String password,
            @Value("${spring.redis.db:0}") Integer db) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        return new JedisPool(poolConfig, host, port, 2000, password, db);
    }
}
