package com.ksyun.trade.controller.online;

import com.ksyun.trade.entity.DO.VoucherDo;
import com.ksyun.trade.rest.RestResult;
import com.ksyun.trade.service.VoucherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/online/voucher", produces = {MediaType.APPLICATION_JSON_VALUE})
@Slf4j
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    @RequestMapping("/deduct")
    public RestResult deduct(@RequestBody VoucherDo voucherDo) {
        voucherService.deduct(voucherDo);
        return RestResult.success();
    }
}
