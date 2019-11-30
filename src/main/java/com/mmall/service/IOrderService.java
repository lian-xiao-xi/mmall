package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.domain.CreateOrderVo;
import com.mmall.domain.PageVo;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderVo;

import java.util.List;
import java.util.Map;

public interface IOrderService {
    ServerResponse createOrder(CreateOrderVo vo, int userId);
    ServerResponse getOrderCartProduct(List<Integer> cartIds, int userId);
    ServerResponse<PageInfo<OrderVo>> userOrderList(PageVo page, int userId);
    ServerResponse<OrderVo> userOrderDetail(Long orderNo, Integer userId);
    ServerResponse<String> cancelOrder(Long orderNo, Integer userId);

    ServerResponse<PageInfo<OrderVo>> managerOrderList(PageVo page);
    ServerResponse<OrderVo> managerOrderDetail(Long orderNo);
    ServerResponse<PageInfo<OrderVo>> managerOrderSearch(Long orderNo, PageVo pageVo);
    ServerResponse<String> manageSendProduct(Long orderNo);

    ServerResponse<Map<String, String>> pay(long orderNo, int userId);
    ServerResponse<String> alipayCallback(Map<String, String> params);
    ServerResponse<String> queryOrderPayStatus(long orderNo, int userId);
}
