package com.ksyun.trade.service;

import com.ksyun.trade.dao.VoucherMapper;
import com.ksyun.trade.entity.DO.VoucherDo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VoucherService {

    @Autowired
    private VoucherMapper voucherMapper;

    public void deduct(VoucherDo voucherDo) {
        voucherMapper.insertVoucher(voucherDo);
    }
}
