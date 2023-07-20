package com.ksyun.trade.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class GatewayService {

    @Value("${actions}")
    private String actions;

    @Autowired
    private HttpServletRequest request;

    public Object loadLalancing(Object param, String interfaceName) {
        // 1. 模拟路由 (负载均衡) 获取接口
        String url = random();
        // 2. 请求转发
        String res = forwarding(url, param, interfaceName);
        return res;
    }

    private String forwarding(String url, Object param, String interfaceName) {
        // 使用UriComponentsBuilder构建URL
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url)
                .pathSegment("online", interfaceName);

        // 将参数添加到URL的路径中
        if (param != null) {
            uriBuilder.pathSegment(param.toString());
        }

        RestTemplate restTemplate = new RestTemplate();
        URI uri = uriBuilder.build().toUri();
        return restTemplate.getForObject(uri, String.class);
    }

    // 随机算法
    private String random() {
        if (actions == null || actions.isEmpty()) {
            return "";
        }
        String[] action = actions.split(",");
        int index = ThreadLocalRandom.current().nextInt(action.length);
        return action[index];
    }
}
