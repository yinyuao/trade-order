package com.ksyun.trade.entity.DO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class ConfigDo {

    @JsonIgnore
    private Integer id;

    private String itemNo;

    private String itemName;

    private String unit;

    private Integer value;
}
