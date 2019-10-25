package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVo;

public interface IProductServer {
    ServerResponse<String> saveOrUpdateProduct(Product product);
    ServerResponse<String> setProductStatus(Integer productId, Integer status);
    ServerResponse<ProductDetailVo> getProductDetail(Integer productId);
}
