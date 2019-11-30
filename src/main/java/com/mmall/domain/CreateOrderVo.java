package com.mmall.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateOrderVo {

    private Integer shippingId;
    private List<OrderProductInfoVo> productList;

}
