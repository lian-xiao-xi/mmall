package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVo;

import java.util.List;

public interface ICartService {
    ServerResponse<CartVo> getCartVoList(int userId);
    ServerResponse<Integer> add(Integer userId, Integer productId, Integer count);
    ServerResponse<Integer> update(Integer userId, Integer productId, Integer count);
    ServerResponse<String> deleteProduct(Integer userId, String productIds);
}
