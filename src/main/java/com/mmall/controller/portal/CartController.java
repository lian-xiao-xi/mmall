package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Cart;
import com.mmall.pojo.User;
import com.mmall.service.impl.CartServiceImpl;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
public class CartController {
    @Autowired
    private CartServiceImpl cartService;

    @RequestMapping(value = "list.do", method = RequestMethod.GET)
    public ServerResponse<CartVo> getCartList(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        return cartService.getCartVoList(user.getId());
    }
}
