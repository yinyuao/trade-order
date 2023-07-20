package com.ksyun.trade.entity.DO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductDo {

    private Integer id;

    private String itemNo;

    private String itemName;

    private String unit;

    private Integer value;
}
