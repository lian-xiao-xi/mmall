package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/shipping")
public class ShippingController {
    @Autowired
    private IShippingService shippingService;

    @RequestMapping(value = "add.do", method = RequestMethod.POST)
    public ServerResponse<Map> addShip(Shipping shipping, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        return shippingService.addShip(user.getId(), shipping);
    }

    @RequestMapping(value ="del.do", method = RequestMethod.DELETE)
    public ServerResponse<String> delShip(Integer shippingId, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        return shippingService.delShip(user.getId(), shippingId);
    }

    @RequestMapping(value ="update.do", method = RequestMethod.PUT)
    public ServerResponse<String> updateShip(Shipping shipping, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        return shippingService.updateShip(user.getId(), shipping);
    }

    @RequestMapping(value = "select.do/{shippingId}", method = RequestMethod.GET)
    public ServerResponse<Shipping> detail(@PathVariable("shippingId") Integer shippingId, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        return shippingService.detail(user.getId(), shippingId);
    }

    @RequestMapping(value = "list.do", method = RequestMethod.GET)
    public ServerResponse<PageInfo<Shipping>> pageList(@RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue = "10") Integer size, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        return shippingService.pageList(user.getId(), page, size);
    }
}
