package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductServer;
import com.mmall.service.IUserService;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import com.sun.istack.internal.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;

@Controller
public class ProductManagerController {

    @Autowired
    private IProductServer iProductServer;

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IFileService iFileService;


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
    public ServerResponse<String> setProductStatus(@RequestParam Integer productId, @RequestParam Integer status, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError("未登录");
        if(iUserService.isAdminRole(user).isSuccess()) {
            return iProductServer.setProductStatus(productId, status);
        } else {
            return ServerResponse.createByError("用户无权限操作");
        }
    }
    
    // 产品详情
    @RequestMapping(value = "detail.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<ProductDetailVo> getProductDetail(@RequestParam Integer productId, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError("未登录");
        if(iUserService.isAdminRole(user).isSuccess()) {
            return iProductServer.getProductDetail(productId);
        } else {
            return ServerResponse.createByError("用户无权限操作");
        }
    }

    // 产品列表
    @RequestMapping(value = "list.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo<ProductListVo>> getProductList(@RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError("未登录");
        if(iUserService.isAdminRole(user).isSuccess()) {
            return iProductServer.getProductList(pageNum, pageSize);
        } else {
            return ServerResponse.createByError("用户无权限操作");
        }
    }
    
    // 搜索产品
    @RequestMapping(value = "search.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo<ProductListVo>> searchProductList(String productName, Integer productId, @RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError("未登录");
        if(iUserService.isAdminRole(user).isSuccess()) {
            return iProductServer.searchProductList(productName, productId, pageNum, pageSize);
        } else {
            return ServerResponse.createByError("用户无权限操作");
        }
    }
    
    // 上传图片
    @RequestMapping(value = "upload.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse upload(@RequestParam(required = false) MultipartFile file, HttpServletRequest httpServletRequest, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) return ServerResponse.createByError("未登录");
        if(iUserService.isAdminRole(user).isSuccess()) {
            String path = session.getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file, path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            HashMap<String, String> fileMap = new HashMap<>();
            fileMap.put("uri", targetFileName);
            fileMap.put("url", url);
            return ServerResponse.createBySuccess(fileMap);
        } else {
            return ServerResponse.createByError("用户无权限操作");
        }
    }
    
}
