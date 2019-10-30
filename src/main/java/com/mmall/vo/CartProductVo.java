package com.mmall.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
public class CartProductVo implements Serializable {
    private Integer id;
    private Integer userId;
    private Integer productId;
    private int quantity;
    private String productName;
    private String productSubtitle;
    private String productMainImage;
    private BigDecimal productPrice;
    private BigDecimal productTotalPrice; // 这个也可以前端计算
    private int productStock;
    private int productStatus;
//    private String limitQuantity;//限制数量的一个返回结果

//    private Integer productChecked;//此商品是否勾选
}
