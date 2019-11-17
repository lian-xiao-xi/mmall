package com.mmall.service.impl;

import com.mmall.common.ServerResponse;
import com.mmall.dao.OrderMapper;
import com.mmall.pojo.Order;
import com.mmall.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService implements IOrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Override
    public ServerResponse pay(long orderNo, int userId, String path) {
        Order order = orderMapper.selectByUserIdAndOrderNo(orderNo, userId);
        if(order == null) return ServerResponse.createByError("用户没有该订单");
        return null;
    }
}
