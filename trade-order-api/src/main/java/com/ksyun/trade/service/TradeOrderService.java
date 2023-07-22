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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private Cache<String, String> twoLevelCache;

    /**
     * 根据订单ID查询订单信息及关联数据
     *
     * @param id 订单ID
     * @return 包含订单信息及关联数据的对象
     */
    public Object query(Integer id) {
        // 生成缓存的key
        String key = "order" + id;
        String cachedData = twoLevelCache.get(key);

        // 如果缓存存在就直接取出来
        if (cachedData != null) {
            // json 转实体类
            OrderDo orderDo = jacksonMapper.fromJson(cachedData, OrderDo.class);
            return orderDo;
        }

        // 从数据库中获取订单信息
        OrderDo orderDo = getOrderFromDatabase(id);

        // 获取远程用户数据
        UserDo userDo = getRemoteUserData(orderDo.getUserId());

        // 获取订单配置信息
        ConfigDo configDo = getOrderConfig(orderDo.getId());

        // 设置订单的配置信息及用户信息
        orderDo.setConfigDo(configDo);
        orderDo.setUserDo(userDo);

        // 获取地区数据列表
        List<RegionDo> list = getRegionDataList();

        // 查找符合条件的RegionDo元素，并设置到orderDo中
        setMatchingRegion(orderDo, list);

        // 将数据存入缓存
        cacheOrderData(key, orderDo);

        return orderDo;
    }

    // 从数据库中获取订单信息
    private OrderDo getOrderFromDatabase(Integer id) {
        OrderDo orderDo = orderMapper.getOrderById(id);
        // 设置订单的upsteam为请求头中的Host信息
        orderDo.setUpsteam(request.getHeader("Host"));
        return orderDo;
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
        String jsonRegions = twoLevelCache.get(url + "/online/region");
        List<RegionDo> list = new ArrayList<>();
        if (jsonRegions != null) {
            list = jacksonMapper.fromJson(jsonRegions, new TypeReference<List<RegionDo>>() {});
        } else {
            // 获取远程地区数据列表
            Map<String, Object> regionMap = RemoteRequestUtils.getRemoteData(url, null, "online", "region", "list");
            // 将地区数据列表映射为List<RegionDo>对象
            list = objectMapper.convertValue(regionMap.get("data"), new TypeReference<List<RegionDo>>() {});
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

    // 将数据存入缓存
    private void cacheOrderData(String key, OrderDo orderDo) {
        twoLevelCache.put(key, jacksonMapper.toJson(orderDo));
    }
}
