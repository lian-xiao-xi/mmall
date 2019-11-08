package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;

import java.util.Map;

public interface IShippingService {
    ServerResponse<Map> addShip(int userId, Shipping shipping);
    ServerResponse<String> delShip(int userId, Integer shippingId);
    ServerResponse<String> updateShip(int userId, Shipping shipping);
    ServerResponse<Shipping> detail(int userId, Integer shippingId);
    ServerResponse<PageInfo<Shipping>> pageList(int userId, int page, int size);
}
