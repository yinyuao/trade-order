package com.ksyun.trade.entity.DO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class OrderDo {

    private String upsteam;

    private Integer id;

    @JsonIgnore
    private Integer userId;

    @JsonIgnore
    private Integer regionId;

    @JsonIgnore
    private Integer productId;

    private BigDecimal priceValue;

    @JsonProperty("user")
    private UserDo userDo;

    @JsonProperty("region")
    private RegionDo regionDo;

    @JsonProperty("configs")
    private ConfigDo configDo;
}
