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
        // 检查优惠券是否已经存在
        Integer count = voucherMapper.checkVoucherNoExists(voucherDo.getVoucherNo());
        if(count == 0) {
            // 生成用于取缓存订单数据的key
            String key = "order-" + voucherDo.getOrderId();

            // 从缓存中检索订单数据
            OrderDo orderDo;
            String cachedData = twoLevelCache.get(key);
            if (cachedData != null) {
                // 从缓存或数据库中检索订单数据
                orderDo = jacksonMapper.fromJson(cachedData, OrderDo.class);
            } else {
                orderDo = orderMapper.getOrderById(voucherDo.getOrderId());
            }

            BigDecimal priceValue = orderDo.getPriceValue();

            // 计算优惠券的抵扣前和抵扣后的金额
            BigDecimal beforeDeductAmount = priceValue.subtract(voucherMapper.getAllDeductAmountByOrderId(voucherDo.getOrderId()));
            voucherDo.setBeforeDeductAmount(beforeDeductAmount);
            voucherDo.setAfterDeductAmount(beforeDeductAmount.subtract(voucherDo.getAmount()));

            // 将优惠券数据插入数据库
            voucherMapper.insertVoucher(voucherDo);
        } else {
            throw new DuplicateKeyException("优惠卷重复！");
        }
    }
}
