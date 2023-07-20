package com.ksyun.trade.entity.DO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class OrderDo {

    private Integer id;

    private String upsteam;

    private Integer userId;

    private Integer regionId;

    private Integer productId;

    private BigDecimal priceValue;

    private UserDo userDo;

    private RegionDo regionDo;

    private ProductDo productDo;
}
