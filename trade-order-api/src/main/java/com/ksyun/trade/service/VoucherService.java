package com.ksyun.trade.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ksyun.common.util.mapper.JacksonMapper;
import com.ksyun.trade.cache.Cache;
import com.ksyun.trade.cache.TwoLevelCache;
import com.ksyun.trade.dao.OrderMapper;
import com.ksyun.trade.dao.VoucherMapper;
import com.ksyun.trade.entity.DO.OrderDo;
import com.ksyun.trade.entity.DO.VoucherDo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.io.IOError;
import java.math.BigDecimal;

@Service
public class VoucherService {

    @Autowired
    private VoucherMapper voucherMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private Cache<String, String> twoLevelCache;

    private static final JacksonMapper jacksonMapper = new JacksonMapper(JsonInclude.Include.NON_NULL);

    public void deduct(VoucherDo voucherDo) {
        Integer count = voucherMapper.checkVoucherNoExists(voucherDo.getVoucherNo());
        if(count == 0) {
            // 生成缓存的key
            String key = "order-" + voucherDo.getOrderId();
            String cachedData = twoLevelCache.get(key);
            OrderDo orderDo;
            // 如果缓存存在就直接取出来
            if (cachedData != null) {
                // json 转实体类
                orderDo = jacksonMapper.fromJson(cachedData, OrderDo.class);
            } else {
                orderDo = orderMapper.getOrderById(voucherDo.getOrderId());
            }
            BigDecimal priceValue = orderDo.getPriceValue();
            voucherDo.setAfterDeductAmount(priceValue);
            voucherDo.setBeforeDeductAmount(priceValue.subtract(voucherDo.getAmount()));
            voucherMapper.insertVoucher(voucherDo);
        } else {
            throw new DuplicateKeyException("优惠卷重复！");
        }
    }
}
