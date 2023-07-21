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

    @Autowired
    private Cache<String, String> twoLevelCache;


    /**
     * 根据机房ID 查询机房名称
     * @param regionId 机房ID
     * @return 机房名称
     */
    @Retryable(maxAttempts = 5, include = RuntimeException.class)
    public Object query(Integer regionId) {
        String cacheKey = url + "-regionId-" + regionId;
        String cachedData = twoLevelCache.get(cacheKey);

        if (cachedData == null) {
            Map<String, Object> value = RemoteRequestUtils.getRemoteData(url, regionId, "online", "region", "name");
            int responseCode = (int) value.getOrDefault("code", -1);

            // 检查 code 是否为 200
            if (responseCode != 200) {
                String errorMsg = (String) value.getOrDefault("message", "Error occurred!");
                throw new RuntimeException(errorMsg);
            }

            String data = (String) value.get("data");
            twoLevelCache.put(cacheKey, data);
            return data;
        } else {
            return cachedData;
        }
    }

}
