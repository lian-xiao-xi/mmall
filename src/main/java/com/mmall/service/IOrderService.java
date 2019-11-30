package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.domain.CreateOrderVo;
import com.mmall.vo.OrderVo;

import java.util.List;
import java.util.Map;

public interface IOrderService {
    ServerResponse createOrder(CreateOrderVo vo, int userId);

    ServerResponse<Map<String, String>> pay(long orderNo, int userId);
    ServerResponse<String> alipayCallback(Map<String, String> params);
    ServerResponse<String> queryOrderPayStatus(long orderNo, int userId);
}
