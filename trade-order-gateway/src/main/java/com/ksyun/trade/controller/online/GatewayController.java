package com.ksyun.trade.controller.online;

import com.ksyun.trade.dto.VoucherDeductDTO;
import com.ksyun.trade.service.GatewayService;
import com.ksyun.trade.utils.RedisLeakyBucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayController {
    @Autowired
    private GatewayService gatewayService;

    @Autowired
    private RedisLeakyBucket redisLeakyBucket;

    /**
     * 查询订单详情 (GET)
     */
    @RequestMapping(value = "/online/queryOrderInfo", produces = "application/json")
    public Object queryOrderInfo(Integer id) {
        return gatewayService.loadBalancing(id, "online", "trade_order");
    }

    /**
     * 根据机房Id查询机房名称 (GET)
     */
    @RequestMapping(value = "/online/queryRegionName", produces = "application/json")
    public Object queryRegionName(Integer regionId) {
        return gatewayService.loadBalancing(regionId, "online", "region");
    }

    /**
     * 订单优惠券抵扣 (POST json)
     */
    @RequestMapping(value = "/online/voucher/deduct", produces = "application/json")
    public Object deduct(@RequestBody VoucherDeductDTO param) {
        return gatewayService.loadBalancing(param, "online", "voucher", "deduct");
    }

    /**
     * 基于Redis实现漏桶限流算法，并在API调用上体现
     */
    @RequestMapping(value = "/online/listUpstreamInfo", produces = "application/json")
    public ResponseEntity<Object> listUpstreamInfo() {
        return redisLeakyBucket.leakyBucket();
    }

}
