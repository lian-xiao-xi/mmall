package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;
import com.mmall.service.ICategroyService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/manage/category")
public class CategoryManagerController {
    @Autowired
    private ICategroyService iCategroyService;

    @Autowired
    private IUserService iUserService;

    // 添加品类
    @RequestMapping(value = "/add_category.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> addCategory(String categoryName, @RequestParam(defaultValue = "0") Integer parentId, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        if(iUserService.isAdminRole(user).isSuccess()) {
            // 是管理员
            return iCategroyService.addCategory(categoryName, parentId);
        } else {
            return ServerResponse.createByError("当前用户无权限操作");
        }
    }

    // 修改品类名字
    @RequestMapping(value = "/set_category_name.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> setCategoryName(String categoryName, Integer categoryId, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        if(iUserService.isAdminRole(user).isSuccess()) {
            return iCategroyService.updateCategory(categoryName, categoryId);
        } else {
            return ServerResponse.createByError("当前用户无权限操作");
        }
    }
    
    // 获取子品类列表
    @RequestMapping(value = "/get_child_categorys.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<List<Category>> getChildCategorys(Integer categoryId, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        if(iUserService.isAdminRole(user).isSuccess()) {
            return iCategroyService.getChildCategorys(categoryId);
        } else {
            return ServerResponse.createByError("当前用户无权限操作");
        }
    }
    
    @RequestMapping(value = "/get_child_deep_category_ids.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<List<Integer>> getChildDeepCategoryIds(Integer categoryId, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        if(iUserService.isAdminRole(user).isSuccess()) {
            return iCategroyService.getChildDeepCategoryIds(categoryId);
        } else {
            return ServerResponse.createByError("当前用户无权限操作");
        }
    }
}
