package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVo;

import java.util.List;

public interface CartService {
    ServerResponse<CartVo> getCartVoList(int userId);
}
