package com.ksyun.trade.cache;

import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisCache<K, V> implements Cache<K, V> {

    private final Jedis jedis;  // Redis客户端

    public RedisCache(Jedis jedis) {
        this.jedis = jedis;
    }

    @Override
    public V get(K key) {
        String value = jedis.get(key.toString());  // 从Redis中获取缓存项
        if (value != null) {
            return (V) value;
        }
        return null;
    }

    @Override
    public void put(K key, V value) {
        jedis.set(key.toString(), value.toString());  // 将缓存项存储到Redis中
    }
}
