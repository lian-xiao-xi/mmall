package com.mmall.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Setter
@Getter
public class OrderVo {
    private Long orderNo;
    private BigDecimal payment;
    private Integer paymentType;
    private Integer postage;
    private Integer status;
    private String statusDesc;
    private Date paymentTime;
    private Date sendTime;
    private Date endTime;
    private Date closeTime;
    private Date createTime;

    //订单的明细
    private List<OrderItemVo> orderItemVoList;

    // 收货地址
    private ShippingVo shippingVo;

    // 图片服务器地址前缀
    private String imageHost;

}
