package com.mmall.service;

import com.mmall.common.ServerResponse;

import java.util.Map;

public interface IOrderService {
    ServerResponse<Map<String, String>> pay(long orderNo, int userId);
}
