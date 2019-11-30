package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.domain.PageVo;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import com.mmall.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/manager/order")
public class OrderManagerController {
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IOrderService iOrderService;

    @RequestMapping(value = "list.do", method = RequestMethod.GET)
    public ServerResponse<PageInfo<OrderVo>> orderList(PageVo pageVo, HttpSession session) {
        ServerResponse<PageInfo<OrderVo>> userServiceAdminRole = iUserService.isAdminRole(session);
        if(!userServiceAdminRole.isSuccess()) return userServiceAdminRole;
        return iOrderService.managerOrderList(pageVo);
    }

    @RequestMapping(value = "detail.do", method = RequestMethod.GET)
    public ServerResponse<OrderVo> orderDetail(@RequestParam Long orderNo, HttpSession session) {
        ServerResponse<OrderVo> userServiceAdminRole = iUserService.isAdminRole(session);
        if(!userServiceAdminRole.isSuccess()) return userServiceAdminRole;
        return iOrderService.managerOrderDetail(orderNo);
    }

    @RequestMapping(value = "search.do", method = RequestMethod.GET)
    public ServerResponse<OrderVo> searchOrderList(@RequestParam Long orderNo, HttpSession session) {
        ServerResponse<OrderVo> userServiceAdminRole = iUserService.isAdminRole(session);
        if(!userServiceAdminRole.isSuccess()) return userServiceAdminRole;
        return iOrderService.managerOrderDetail(orderNo);
    }

    @RequestMapping(value = "send_product.do", method = RequestMethod.POST)
    public ServerResponse<String> sendOrderProduct(@RequestParam Long orderNo, HttpSession session) {
        ServerResponse<String> userServiceAdminRole = iUserService.isAdminRole(session);
        if(!userServiceAdminRole.isSuccess()) return userServiceAdminRole;
        return iOrderService.manageSendProduct(orderNo);
    }
}
