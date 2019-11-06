package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.service.IProductServer;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private IProductServer iProductServer;

    @RequestMapping(value = "detail.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<ProductDetailVo> detail(Integer productId) {
        return iProductServer.portalProductDetail(productId);
    }

    // 产品搜索
    @RequestMapping(value = "list.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo<ProductListVo>> list(@RequestParam(required = false) String productName,
                                                        @RequestParam(required = false) Integer categoryId,
                                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                                        String orderBy) {
        return iProductServer.portalGetProductByKeywordCategory(productName, categoryId, pageNum, pageSize, orderBy);
    }
}
