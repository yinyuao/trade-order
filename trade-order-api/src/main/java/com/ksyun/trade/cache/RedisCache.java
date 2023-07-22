package com.ksyun.trade.cache;

import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisCache<K, V> implements Cache<K, V> {

    private final JedisPool jedisPool;

    public RedisCache(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public V get(K key) {
        try (Jedis jedis = jedisPool.getResource()) {
            String value = jedis.get(key.toString());  // 从Redis中获取缓存项
            if (value != null) {
                return (V) value;
            }
            return null;
        }
    }

    @Override
    public void put(K key, V value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key.toString(), value.toString());  // 将缓存项存储到Redis中
        }
    }
}
