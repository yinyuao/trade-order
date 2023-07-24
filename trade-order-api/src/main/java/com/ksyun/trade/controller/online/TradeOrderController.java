package com.ksyun.trade.controller.online;

import com.ksyun.trade.rest.RestResult;
import com.ksyun.trade.service.TradeOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(value = "/online/trade_order", produces = {MediaType.APPLICATION_JSON_VALUE})
@Slf4j
public class TradeOrderController {
    @Autowired
    private TradeOrderService orderService;

    @RequestMapping("/{id}")
    public RestResult query(@PathVariable("id") Integer id, HttpServletRequest reques) {
        CompletableFuture<Object> resultFuture = orderService.query(id, reques);
        Object result = resultFuture.join();
        return RestResult.success().data(result);
    }

}
