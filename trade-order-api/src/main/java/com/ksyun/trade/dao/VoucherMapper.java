package com.ksyun.trade.dao;

import com.ksyun.trade.entity.DO.VoucherDo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VoucherMapper {

    // 插入VoucherDo对象
    @Insert("INSERT INTO ksc_voucher_deduct (order_id, voucher_no, amount, before_deduct_amount, after_deduct_amount) " +
            "VALUES (#{orderId}, #{voucherNo}, #{amount}, #{beforeDeductAmount}, #{afterDeductAmount})")
    void insertVoucher(VoucherDo voucherDo);

    // 检查voucher_no是否重复
    @Select("SELECT COUNT(*) FROM ksc_voucher_deduct WHERE voucher_no = #{voucherNo}")
    Integer checkVoucherNoExists(String voucherNo);
}
