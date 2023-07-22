package com.ksyun.trade.dao;

import com.ksyun.trade.entity.DO.VoucherDo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VoucherMapper {

    // 插入VoucherDo对象
    @Insert("INSERT INTO ksc_voucher_deduct (order_id, voucher_no, amount) " +
            "VALUES (#{orderId}, #{voucherNo}, #{amount})")
    void insertVoucher(VoucherDo voucherDo);

}
