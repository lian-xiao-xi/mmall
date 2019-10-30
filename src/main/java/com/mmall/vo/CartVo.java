package com.mmall.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CartVo implements Serializable {
    private List<CartProductVo> cartProductVoList;
//    private BigDecimal cartTotalPrice; // 前端计算
//    private Boolean allChecked;//是否已经都勾选
//    private String imageHost;
}
