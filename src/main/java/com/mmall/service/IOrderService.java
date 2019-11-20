package com.mmall.service;

import com.mmall.common.ServerResponse;

import java.util.Map;

public interface IOrderService {
    ServerResponse<Map<String, String>> pay(long orderNo, int userId);
    ServerResponse<String> alipayCallback(Map<String, String> params);
    ServerResponse<String> queryOrderPayStatus(long orderNo, int userId);
}
