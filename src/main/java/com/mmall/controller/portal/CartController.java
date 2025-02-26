package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.impl.CartServiceImpl;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartServiceImpl iCartService;

    // 获取用户购物车列表
    @RequestMapping(value = "list.do", method = RequestMethod.GET)
    public ServerResponse<CartVo> getCartList(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        return iCartService.getCartVoList(user.getId());
    }

    // 添加商品到购物车
    @RequestMapping(value = "add.do", method = RequestMethod.POST)
    public ServerResponse addProductToCart(Integer productId, Integer count, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.add(user.getId(), productId, count);

    }

    // 更新购物车商品数量
    @RequestMapping(value = "update.do", method = RequestMethod.POST)
    public ServerResponse<Integer> updateCartProductCount(Integer productId, Integer count, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.update(user.getId(), productId, count);

    }

    // 删除、批量删除
    @RequestMapping(value = "delete.do", method = RequestMethod.POST)
    public ServerResponse<String> deleteCartProduct(@RequestParam List<Integer> cartIds, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.deleteProduct(user.getId(), cartIds);
    }
}
