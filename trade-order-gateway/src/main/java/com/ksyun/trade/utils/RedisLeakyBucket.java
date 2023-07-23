package com.ksyun.trade.utils;

import com.ksyun.trade.rest.RestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class RedisLeakyBucket {
    private static final String REDIS_KEY = "leaky_bucket";
    private static final int BUCKET_CAPACITY = 5; // 漏桶最大容量
    private static final int DRIP_RATE_MS = 1000; // 滴漏速率（每秒1个请求）
    private static UUID uuid = UUID.randomUUID();

    @Autowired
    private JedisPool jedisPool;

    @Value("${actions}")
    private String actions;

    /**
     * 漏桶算法的实现方法。
     * 每当有请求到达时，通过 Redis 的有序集合来模拟一个漏桶。
     * 如果漏桶中的请求数没有超过最大容量 BUCKET_CAPACITY，则将当前请求的时间戳加入有序集合，
     * 并返回成功的响应，表示请求被允许通过。
     * 如果漏桶中的请求数已经达到最大容量，则拒绝新的请求，返回限流的错误响应。
     *
     * @return 请求通过时的响应数据，或者限流时的错误响应数据
     */
    public ResponseEntity<Object> leakyBucket() {
        try (Jedis jedis = jedisPool.getResource()) {

            long currentTime = System.currentTimeMillis();
            // 移除有序集合中分数（时间戳）小于当前时间 - DRIP_RATE_MS 的元素
            jedis.zremrangeByScore(REDIS_KEY, 0, currentTime - DRIP_RATE_MS);

            // 获取当前有序集合的元素数量，即漏桶中的请求数
            long bucketSize = jedis.zcard(REDIS_KEY);

            if (bucketSize < BUCKET_CAPACITY) {
                // 如果桶有空间，将当前请求的时间戳加入有序集合，并返回成功的响应
                jedis.zadd(REDIS_KEY, currentTime, String.valueOf(currentTime));
                return getResponseData();
            } else {
                // 否则，由于限流拒绝请求，返回限流的错误响应
                return getErrorResponse();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return getErrorResponse();
        }

    }

    /**
     * 构建请求通过时的响应数据。
     *
     * @return 响应数据
     */
    private ResponseEntity<Object> getResponseData() {
        // 成功时返回
        Map<String, Object> res = new LinkedHashMap<>();
        String[] action = actions.split(",");
        res.put("code", 200);
        res.put("msg", "ok");
        res.put("requestId", uuid);
        res.put("data", action);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    /**
     * 构建限流时的错误响应数据。
     *
     * @return 错误响应数据
     */
    // 在漏桶算法的实现中，当限流生效时，返回 429 状态码
    private ResponseEntity<Object> getErrorResponse() {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("code", 429);
        res.put("requestId", uuid);
        res.put("msg", "对不起，系统压力过大，请稍后再试!");
        // 使用 ResponseEntity 设置响应状态码为 429，并返回错误响应
        return new ResponseEntity<>(res, HttpStatus.TOO_MANY_REQUESTS);
    }
}
