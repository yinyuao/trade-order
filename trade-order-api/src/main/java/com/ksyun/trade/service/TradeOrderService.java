package com.ksyun.trade.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.ksyun.common.util.mapper.JacksonMapper;
import com.ksyun.trade.cache.Cache;
import com.ksyun.trade.dao.OrderMapper;
import com.ksyun.trade.dao.ConfigMapper;
import com.ksyun.trade.entity.DO.OrderDo;
import com.ksyun.trade.entity.DO.ConfigDo;
import com.ksyun.trade.entity.DO.RegionDo;
import com.ksyun.trade.entity.DO.UserDo;
import com.ksyun.trade.utils.RemoteRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class TradeOrderService {

    @Value("${meta.url}")
    private String url;

    private static final JacksonMapper jacksonMapper = new JacksonMapper(JsonInclude.Include.NON_NULL);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ConfigMapper configMapper;

//    @Autowired
//    private HttpServletRequest request;

    @Autowired
    private Cache<String, String> twoLevelCache;

    @Autowired
    @Qualifier("asyncTaskExecutor")
    private ThreadPoolTaskExecutor asyncTaskExecutor;

    /**
     * 根据订单ID查询订单信息及关联数据
     *
     * @param id     订单ID
     * @param reques
     * @return 包含订单信息及关联数据的对象
     */
    @Async("asyncTaskExecutor")
    public CompletableFuture<Object> query(Integer id, HttpServletRequest reques) {
        // 生成缓存的key
        String key = "order-" + id;
        String cachedData = twoLevelCache.get(key);

        // 如果缓存存在就直接取出来
        if (cachedData != null) {
            // json 转实体类
            OrderDo orderDo = jacksonMapper.fromJson(cachedData, OrderDo.class);
            return CompletableFuture.completedFuture(orderDo);
        }

        // 从数据库中获取订单信息
        OrderDo orderDo = getOrderFromDatabase(id);

        // 从数据库获取配置信息
        ConfigDo configDo = getOrderConfig(orderDo.getId());

        CompletableFuture<UserDo> userFuture = getRemoteUserDataAsync(orderDo.getUserId());
        CompletableFuture<List<RegionDo>> regionFuture = getRegionDataListAsync();

        // 等待两个异步任务完成
        CompletableFuture.allOf(userFuture, regionFuture).join();

        UserDo userDo = userFuture.join();
        List<RegionDo> regionList = regionFuture.join();

        // 设置订单的配置信息及用户信息
        orderDo.setConfigDo(configDo);
        orderDo.setUserDo(userDo);

        // 获取地区数据列表
        List<RegionDo> list = getRegionDataList();

        // 查找符合条件的RegionDo元素，并设置到orderDo中
        setMatchingRegion(orderDo, list);

        // 设置订单的upsteam为请求头中的Host信息
        orderDo.setUpsteam(reques.getHeader("Host"));

        // 将数据存入缓存
        twoLevelCache.put(key, jacksonMapper.toJson(orderDo));

        return CompletableFuture.completedFuture(orderDo);
    }

    // 从数据库中获取订单信息
    private OrderDo getOrderFromDatabase(Integer id) {
        OrderDo orderDo = orderMapper.getOrderById(id);
        return orderDo;
    }

    private CompletableFuture<UserDo> getRemoteUserDataAsync(Integer userId) {
        return CompletableFuture.supplyAsync(() -> {
            // 异步获取远程用户数据
            return getRemoteUserData(userId);
        }, asyncTaskExecutor);
    }

    private CompletableFuture<List<RegionDo>> getRegionDataListAsync() {
        return CompletableFuture.supplyAsync(() -> {
            // 异步获取地区数据列表
            return getRegionDataList();
        }, asyncTaskExecutor);
    }

    // 获取远程用户数据
    private UserDo getRemoteUserData(Integer userId) {
        Map<String, Object> userMap = RemoteRequestUtils.getRemoteData(url, userId, "online", "user");
        return objectMapper.convertValue(userMap.get("data"), UserDo.class);
    }

    // 获取订单配置信息
    private ConfigDo getOrderConfig(Integer orderId) {
        return configMapper.getConfigById(orderId);
    }

    // 获取地区数据列表
    private List<RegionDo> getRegionDataList() {
        String key = url + "/online/region";
        String jsonRegions = twoLevelCache.get(key);
        List<RegionDo> list = new ArrayList<>();
        if (jsonRegions != null) {
            list = jacksonMapper.fromJson(jsonRegions, new TypeReference<List<RegionDo>>() {});
        } else {
            // 获取远程地区数据列表
            Map<String, Object> regionMap = RemoteRequestUtils.getRemoteData(url, null, "online", "region", "list");
            // 将地区数据列表映射为List<RegionDo>对象
            list = objectMapper.convertValue(regionMap.get("data"), new TypeReference<List<RegionDo>>() {});

            // 放入缓存
            String jsonList = jacksonMapper.toJson(list);
            twoLevelCache.put(key, jsonList);
        }
        return list;
    }

    // 查找符合条件的RegionDo元素，并设置到orderDo中
    private void setMatchingRegion(OrderDo orderDo, List<RegionDo> list) {
        list.stream()
                .filter(region -> Objects.equals(region.getId(), orderDo.getRegionId()))
                .findFirst()
                .ifPresent(region -> {
                    // 设置地区的id为null，以实现不显示id
                    region.setId(null);
                    orderDo.setRegionDo(region);
                });
    }

}
