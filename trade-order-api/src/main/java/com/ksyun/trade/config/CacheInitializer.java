package com.ksyun.trade.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksyun.trade.cache.Cache;
import com.ksyun.trade.cache.MemoryCache;
import com.ksyun.trade.cache.RedisCache;
import com.ksyun.trade.cache.TwoLevelCache;
import com.ksyun.trade.entity.DO.RegionDo;
import com.ksyun.trade.utils.RemoteRequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.Map;

@Component
public class CacheInitializer implements ApplicationRunner {

    @Value("${meta.url}")
    private String url;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JedisPool jedisPool;

    // 删除初始化远程数据列表
    @Override
    public void run(ApplicationArguments args) {
//        // 获取远程地区数据列表
//        Map<String, Object> regionMap = RemoteRequestUtils.getRemoteData(url, null, "online", "region", "list");
//        // 将地区数据列表映射为List<RegionDo>对象
//        List<RegionDo> list = objectMapper.convertValue(regionMap.get("data"), new TypeReference<List<RegionDo>>() {});
//
//        Jedis jedis = jedisPool.getResource();
//        try {
//            String key = url + "/online/region";
//            String jsonList = objectMapper.writeValueAsString(list);
//            jedis.set(key, jsonList);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }

    }
}
