package com.ksyun.trade.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class GatewayService {

    // 从配置文件中读取接口URL列表，以逗号分隔
    @Value("${actions}")
    private String actions;

    /**
     * 负载均衡器，根据路由规则选择一个接口URL进行请求转发
     *
     * @param param         请求参数对象
     * @param interfaceNames 接口名称
     * @return 转发后接口的响应结果
     */
    public Object loadBalancing(Object param, String... interfaceNames) {
        // 1. 模拟路由 (负载均衡) 获取接口
        String url = random();
        // 2. 请求转发
        String res = forwarding(url, param, interfaceNames);
        return res;
    }

    /**
     * 请求转发方法，根据给定的接口URL和参数，进行请求转发并返回响应结果
     *
     * @param url           接口URL
     * @param param         请求参数对象
     * @param interfaceNames 接口名称
     * @return 转发后接口的响应结果
     */
    private String forwarding(String url, Object param, String... interfaceNames) {
        // 使用UriComponentsBuilder构建URL
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);
        uriBuilder.pathSegment(interfaceNames);

        // 将参数添加到URL的路径中
        if (param != null) {
            uriBuilder.pathSegment(param.toString());
        }

        RestTemplate restTemplate = new RestTemplate();
        URI uri = uriBuilder.build().toUri();
        return restTemplate.getForObject(uri, String.class);
    }

    /**
     * 随机算法，从接口URL列表中随机选择一个URL进行负载均衡
     *
     * @return 随机选择的接口URL
     */
    private String random() {
        if (actions == null || actions.isEmpty()) {
            return "";
        }
        String[] action = actions.split(",");
        int index = ThreadLocalRandom.current().nextInt(action.length);
        return action[index];
    }
}
