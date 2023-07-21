package com.ksyun.trade.cache;

import java.util.HashMap;
import java.util.Map;

public class MemoryCache<K, V> implements Cache<K, V> {
    private final int maxSize;  // 最大缓存容量
    private final long expirationTime;  // 缓存过期时间
    private final Map<K, CacheEntry<V>> cache;  // 缓存数据存储容器

    public MemoryCache(int maxSize, long expirationTime) {
        this.maxSize = maxSize;
        this.expirationTime = expirationTime;
        this.cache = new HashMap<>();
    }

    @Override
    public V get(K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {  // 如果缓存项存在且未过期
            return entry.getValue();
        }
        return null;
    }

    @Override
    public void put(K key, V value) {
        if (cache.size() >= maxSize) {  // 如果缓存已满
            evictExpiredEntries();  // 清除过期的缓存项
            if (cache.size() >= maxSize) {
                evictOldestEntry();  // 如果仍然缓存已满，清除最旧的缓存项
            }
        }
        cache.put(key, new CacheEntry<>(value, expirationTime));  // 将新的缓存项放入缓存容器
    }

    private void evictExpiredEntries() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());  // 清除过期的缓存项
    }

    private void evictOldestEntry() {
        K oldestKey = null;
        long oldestTimestamp = Long.MAX_VALUE;
        for (Map.Entry<K, CacheEntry<V>> entry : cache.entrySet()) {
            long timestamp = entry.getValue().getTimestamp();
            if (timestamp < oldestTimestamp) {  // 找到最旧的缓存项
                oldestKey = entry.getKey();
                oldestTimestamp = timestamp;
            }
        }
        if (oldestKey != null) {
            cache.remove(oldestKey);  // 清除最旧的缓存项
        }
    }

    private static class CacheEntry<V> {
        private final V value;  // 缓存项的值
        private final long timestamp;  // 缓存项的过期时间戳

        public CacheEntry(V value, long expirationTime) {
            this.value = value;
            this.timestamp = System.currentTimeMillis() + expirationTime;  // 计算过期时间戳
        }

        public V getValue() {
            return value;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > timestamp;  // 判断缓存项是否过期
        }
    }
}
