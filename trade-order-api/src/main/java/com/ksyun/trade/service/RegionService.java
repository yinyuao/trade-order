package com.ksyun.trade.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksyun.trade.utils.RemoteRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class RegionService {

    @Value("${meta.url}")
    private String url;

    /**
     * 根据机房ID 查询机房名称
     *
     * @param regionId 机房ID
     * @return 机房名称
     */
    public Object query(Integer regionId) {
        Map<String, Object> value = RemoteRequestUtils.getRemoteData(url, regionId, "online", "region", "name");
        return value.get("data");
    }
}
