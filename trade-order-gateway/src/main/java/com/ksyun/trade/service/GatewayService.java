package com.ksyun.trade.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Maps;
import com.ksyun.common.util.mapper.JacksonMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class GatewayService {

    private static final JacksonMapper jacksonMapper = new JacksonMapper(JsonInclude.Include.NON_NULL);

    @Autowired
    private HttpServletRequest request;

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
        String res = "";
        if ("GET".equals(request.getMethod())) {
            res = forwardingGet(url, param, interfaceNames, getHeader(request));
        } else if ("POST".equals(request.getMethod())){
            res = forwardingPost(url, param, interfaceNames, getHeader(request));
        }
        return res;
    }

    /**
     * get的请求转发方法，根据给定的接口URL和参数，进行请求转发并返回响应结果
     *
     * @param url            接口URL
     * @param param          请求参数对象
     * @param interfaceNames 接口名称
     * @param headers        请求头部信息
     * @return 转发后接口的响应结果
     */
    private String forwardingGet(String url, Object param, String[] interfaceNames, Map<String, String> headers) {
        // 使用UriComponentsBuilder构建URL
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);
        uriBuilder.pathSegment(interfaceNames);

        // 将参数添加到URL的路径中
        if (param != null) {
            uriBuilder.pathSegment(param.toString());
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = createHttpHeaders(headers);

        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        URI uri = uriBuilder.build().toUri();
        return restTemplate.exchange(uri, HttpMethod.GET, entity, String.class).getBody();
    }

    /**
     * post的请求转发方法，根据给定的接口URL和参数，进行请求转发并返回响应结果
     *
     * @param url            接口URL
     * @param param          请求参数对象
     * @param interfaceNames 接口名称
     * @param headers        请求头部信息
     * @return 转发后接口的响应结果
     */
    private String forwardingPost(String url, Object param, String[] interfaceNames, Map<String, String> headers) {
        // 使用UriComponentsBuilder构建URL
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);
        uriBuilder.pathSegment(interfaceNames);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = createHttpHeaders(headers);

        String requestBody = jacksonMapper.toJson(param);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, httpHeaders);

        URI uri = uriBuilder.build().toUri();
        return restTemplate.exchange(uri, HttpMethod.POST, entity, String.class).getBody();
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

    /**
     * 创建 HttpHeaders 对象，将传入的请求头部信息添加到其中
     *
     * @param headers 请求头部信息
     * @return HttpHeaders 对象
     */
    private HttpHeaders createHttpHeaders(Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpHeaders.add(entry.getKey(), entry.getValue());
            }
        }

        return httpHeaders;
    }

    /**
     * 获取请求的头部信息
     *
     * @param request HttpServletRequest对象
     * @return 包含头部信息的Map，其中键为头部名称，值为头部的值
     */
    private Map<String, String> getHeader(HttpServletRequest request) {
        Map<String, String> headers = Maps.newHashMap();
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String header = enumeration.nextElement();
            headers.put(header.toUpperCase(), request.getHeader(header));
        }
        return headers;
    }
}
