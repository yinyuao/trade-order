package com.ksyun.trade.controller.online;

import com.ksyun.trade.rest.RestResult;
import com.ksyun.trade.service.RegionService;
import com.ksyun.trade.service.TradeOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/online/region", produces = {MediaType.APPLICATION_JSON_VALUE})
@Slf4j
public class RegionController {

    @Autowired
    private RegionService regionService;

    @RequestMapping("/{regionId}")
    public RestResult query(@PathVariable("regionId") Integer regionId) throws Exception {
        return RestResult.success().data(regionService.query(regionId));
    }
}
