package com.ksyun.trade.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
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
            try {
                OrderDo orderDo = objectMapper.readValue(cachedData, OrderDo.class);
                return orderDo;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // 从数据库中获取订单信息
        OrderDo orderDo = orderMapper.getOrderById(id);
        // 设置订单的upsteam为请求头中的Host信息
        orderDo.setUpsteam(request.getHeader("Host"));

        // 获取远程用户数据
        Map<String, Object> userMap = RemoteRequestUtils.getRemoteData(url, orderDo.getUserId(), "online", "user");

        // 获取订单配置信息
        ConfigDo configDo = configMapper.getConfigById(orderDo.getId());
        // 将用户数据映射为UserDo对象
        UserDo userDo = objectMapper.convertValue(userMap.get("data"), UserDo.class);

        // 设置订单的配置信息及用户信息
        orderDo.setConfigDo(configDo);
        orderDo.setUserDo(userDo);

        String jsonRegions = twoLevelCache.get(url + "/online/region");
        List<RegionDo> list = new ArrayList<>();

        // 若缓存有数据直接从缓存取数据
        if (jsonRegions != null) {
            try {
                list = objectMapper.readValue(jsonRegions, new TypeReference<List<RegionDo>>() {
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 获取远程地区数据列表
            Map<String, Object> regionMap = RemoteRequestUtils.getRemoteData(url, null, "online", "region", "list");
            // 将地区数据列表映射为List<RegionDo>对象
            list = objectMapper.convertValue(regionMap.get("data"), new TypeReference<List<RegionDo>>() {
            });
        }

        // 查找符合条件的RegionDo元素，并设置到orderDo中
        list.stream()
                .filter(region -> Objects.equals(region.getId(), orderDo.getRegionId()))
                .findFirst()
                .ifPresent(region -> {
                    // 设置地区的id为null，以实现不显示id
                    region.setId(null);
                    orderDo.setRegionDo(region);
                });

        // 将数据存入缓存
        try {
            twoLevelCache.put(key, objectMapper.writeValueAsString(orderDo));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return orderDo;
    }
}