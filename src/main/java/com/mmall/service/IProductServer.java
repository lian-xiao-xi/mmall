package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;

import java.util.List;

public interface IProductServer {
    ServerResponse<String> saveOrUpdateProduct(Product product);
    ServerResponse<String> setProductStatus(Integer productId, Integer status);
    ServerResponse<ProductDetailVo> getProductDetail(Integer productId);
    ServerResponse<PageInfo<ProductListVo>> getProductList(Integer pageNum, Integer pageSize);
    ServerResponse<PageInfo<ProductListVo>> searchProductList(String productName, Integer productId, Integer pageNum, Integer pageSize);
}
