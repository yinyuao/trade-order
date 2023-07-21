package com.ksyun.trade.entity.DO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegionDo {

    private Integer id;

    private String code;

    private String name;

    @JsonIgnore
    private Integer status;

}
