package com.ksyun.trade.entity.DO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class UserDo {

    @JsonIgnore
    private Integer id;

    private String username;

    private String email;

    private String phone;

    private String address;

}
