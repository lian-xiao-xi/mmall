package com.mmall.service.impl;

import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Product;
import com.mmall.service.IProductServer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements IProductServer {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public ServerResponse<String> saveOrUpdateProduct(Product product) {
        if(product == null) return ServerResponse.createByError("参数错误");
        String subImages = product.getSubImages();
        if(StringUtils.isNotBlank(subImages)) {
            String[] subImagesArray = subImages.split(",");
            if(subImagesArray.length > 0) product.setMainImage(subImagesArray[0]);
        }
        if(product.getId() == null) {
            // 添加
            int i = productMapper.insertSelective(product);
//            int i = productMapper.insert(product);
            if(i>0) return ServerResponse.createBySuccessMessage("添加产品成功");
            else return ServerResponse.createByError("添加产品失败");
        } else {
            // 编辑
            int i = productMapper.updateByPrimaryKeySelective(product);
//            int i = productMapper.updateByPrimaryKey(product);
            if(i>0) return ServerResponse.createBySuccessMessage("更新产品成功");
            else return ServerResponse.createByError("更新产品失败");
        }
    }

    @Override
    public ServerResponse<String> setProductStatus(Integer productId, Integer status) {
        /*
        * 我的实现方式
        */
//        Product product = productMapper.selectByPrimaryKey(productId);
//        if(product == null) {
//            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
//        }
//        product.setStatus(status);
//        int i = productMapper.updateByPrimaryKeySelective(product);
//        if(i>0) return ServerResponse.createBySuccessMessage("修改产品状态成功");
//        return ServerResponse.createByError("修改产品状态失败");

        /*
         * 教程的实现方式
         */

        // 我觉得这个没必要判断 status 是否为 null，因为此字段是前端必传的不过也不知道前端传 null 到后端回会是什么
        if(productId == null || status == null) {
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int i = productMapper.updateByPrimaryKeySelective(product);
        if(i>0) return ServerResponse.createBySuccessMessage("修改产品状态成功");
        return ServerResponse.createByError("修改产品状态失败");
    }
}
