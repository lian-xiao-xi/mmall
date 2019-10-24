package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IProductServer;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
public class ProductManagerController {

    @Autowired
    private IProductServer iProductServer;

    @Autowired
    private IUserService iUserService;


    @RequestMapping(value = "save.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> productSave(Product product, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError("未登录");
        if(iUserService.isAdminRole(user).isSuccess()) {
            return iProductServer.saveOrUpdateProduct(product);
        } else {
            return ServerResponse.createByError("用户无权限操作");
        }
    }

    // 设置产品状态 对应文档上的 set_sale_status 接口
    @RequestMapping(value = "set_product_status.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> setProductStatus(Integer productId, Integer status, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError("未登录");
        if(iUserService.isAdminRole(user).isSuccess()) {
            return iProductServer.setProductStatus(productId, status);
        } else {
            return ServerResponse.createByError("用户无权限操作");
        }
    }
}
