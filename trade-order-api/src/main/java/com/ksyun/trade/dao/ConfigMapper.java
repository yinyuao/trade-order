package com.ksyun.trade.dao;

import com.ksyun.trade.entity.DO.ConfigDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ConfigMapper {

    // 根据ID查询产品
    @Select("SELECT * FROM ksc_trade_product_config WHERE id = #{productId}")
    @Results({
            @Result(column = "item_no", property = "itemNo"),
            @Result(column = "item_name", property = "itemName"),
            @Result(column = "unit", property = "unit"),
            @Result(column = "value", property = "value")
    })
    ConfigDo getConfigById(Integer productId);


}
