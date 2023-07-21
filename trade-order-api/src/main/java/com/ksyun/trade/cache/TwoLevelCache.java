package com.ksyun.trade.cache;

public class TwoLevelCache<K, V> implements Cache<K, V> {
    private final Cache<K, V> level1Cache;  // 一级缓存
    private final Cache<K, V> level2Cache;  // 二级缓存

    public TwoLevelCache(Cache<K, V> level1Cache, Cache<K, V> level2Cache) {
        this.level1Cache = level1Cache;
        this.level2Cache = level2Cache;
    }

    @Override
    public V get(K key) {
        V value = level1Cache.get(key);  // 从一级缓存获取缓存项
        if (value == null) {
            value = level2Cache.get(key);  // 从二级缓存获取缓存项
            if (value != null) {
                level1Cache.put(key, value);  // 将缓存项存储到一级缓存
            }
        }
        return value;
    }

    @Override
    public void put(K key, V value) {
        level1Cache.put(key, value);  // 将缓存项存储到一级缓存
        level2Cache.put(key, value);  // 将缓存项存储到二级缓存
    }
}
