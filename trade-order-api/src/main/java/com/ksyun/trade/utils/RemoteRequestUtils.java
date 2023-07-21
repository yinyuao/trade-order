package com.ksyun.trade.utils;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

public class RemoteRequestUtils {
    /**
     * 发起远程请求获取数据
     *
     * @param url   远程IP地址
     * @param id    数据ID（如果有）
     * @param names 请求路径中的名称参数
     * @return 远程返回的数据Map
     */
    public static Map<String, Object> getRemoteData(String url, Integer id, String... names) {
        // 使用UriComponentsBuilder构建URL
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);
        uriBuilder.pathSegment(names);

        // 将参数添加到URL的路径中
        if (id != null) {
            uriBuilder.pathSegment(id.toString());
        }
        RestTemplate restTemplate = new RestTemplate();
        URI uri = uriBuilder.build().toUri();
        return restTemplate.getForObject(uri, Map.class);
    }
}
