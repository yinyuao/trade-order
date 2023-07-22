package com.ksyun.trade.dao;

import com.ksyun.trade.entity.DO.OrderDo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderMapper {

    // 根据ID查询订单
    @Select("SELECT id, user_id, region_id, product_id, price_value FROM ksc_trade_order WHERE id = #{id}")
    @Results({
            @Result(column = "user_id", property = "userId"),
            @Result(column = "region_id", property = "regionId"),
            @Result(column = "product_id", property = "productId"),
            @Result(column = "price_value", property = "priceValue")
    })
    OrderDo getOrderById(Integer id);

}
