package com.ksyun.trade.entity.DO;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class VoucherDo {

    private Integer orderId;

    private String voucherNo;

    private BigDecimal amount;

    private BigDecimal beforeDeductAmount;

    private BigDecimal afterDeductAmount;

}
