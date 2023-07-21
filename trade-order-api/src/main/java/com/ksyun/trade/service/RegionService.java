package com.ksyun.trade.service;

import com.ksyun.trade.cache.Cache;
import com.ksyun.trade.cache.MemoryCache;
import com.ksyun.trade.cache.RedisCache;
import com.ksyun.trade.cache.TwoLevelCache;
import com.ksyun.trade.utils.RemoteRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@EnableRetry
public class RegionService {

    @Value("${meta.url}")
    private String url;

    private final Cache<String, String> twoLevelCache;

    @Autowired
    public RegionService(@Value("${spring.redis.host}") String host,
                         @Value("${spring.redis.port}") Integer port,
                         @Value("${spring.redis.password}") String password,
                         @Value("${spring.redis.db}") Integer db) {
        Cache<String, String> memoryCache = new MemoryCache<>(1000, 60 * 60 * 1000);
        Cache<String, String> redisCache = new RedisCache<>(host, port, password, db);
        this.twoLevelCache = new TwoLevelCache<>(memoryCache, redisCache);
    }

    /**
     * 根据机房ID 查询机房名称
     * @param regionId 机房ID
     * @return 机房名称
     */
    @Retryable(maxAttempts = 5, include = Exception.class)
    public Object query(Integer regionId) throws Exception {
        String cacheKey = url + "-regionId-" + regionId;
        String cachedData = twoLevelCache.get(cacheKey);
        if (cachedData == null) {
            Map<String, Object> value = RemoteRequestUtils.getRemoteData(url, regionId, "online", "region", "name");
            // 检查value中的code是否为500
            if (value.containsKey("code") && !value.get("code").equals(200)) {
                throw new Exception("出错！");
            }
            String data = (String) value.get("data");
            twoLevelCache.put(cacheKey, data);
            return data;
        } else {
            return cachedData;
        }
    }
}
