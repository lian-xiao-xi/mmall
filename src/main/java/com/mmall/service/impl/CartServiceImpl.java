package com.mmall.service.impl;

import com.mmall.common.BigDecimalUtil;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements ICartService {
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    @Override
    public ServerResponse<CartVo> getCartVoList(int userId) {
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        List<CartProductVo> cartProductVoList = cartList.stream().map(cart -> {
            CartProductVo cartProductVo = new CartProductVo();
            cartProductVo.setId(cart.getId());
            cartProductVo.setProductId(cart.getProductId());
            cartProductVo.setUserId(cart.getUserId());
            cartProductVo.setQuantity(cart.getQuantity());
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            if (product != null) {
                cartProductVo.setProductMainImage(product.getMainImage());
                cartProductVo.setProductName(product.getName());
                cartProductVo.setProductPrice(product.getPrice());
                cartProductVo.setProductStatus(product.getStatus());
                cartProductVo.setProductStock(product.getStock());
                cartProductVo.setProductSubtitle(product.getSubtitle());
                BigDecimal totalPrice = BigDecimalUtil.add(product.getPrice().doubleValue(), cart.getQuantity());
                cartProductVo.setProductTotalPrice(totalPrice);
            }
            return cartProductVo;
        }).collect(Collectors.toList());
        CartVo cartVo = new CartVo();
        cartVo.setCartProductVoList(cartProductVoList);

        return ServerResponse.createBySuccess(cartVo);
    }

    @Override
    public ServerResponse<Integer> add(Integer userId, Integer productId, Integer count) {
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null) {
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Integer stock = product.getStock();
        Cart cart = cartMapper.selectCartByUserIdAndProductId(userId, productId);
        if(cart == null) {
            // 购物车中没有此商品
            // 判断库存与购物车中此商品的数量关系
            if(stock >= count) {
                // 库存充足
                Cart cartInstance = new Cart();
                cartInstance.setQuantity(count);
                cartInstance.setProductId(productId);
                cartInstance.setUserId(userId);
                // 教程数据库中有一个字段表示此购物车商品是否选中；我的方案是不需要这个字段，是否选中即相关计算由前端完成
                // 设置此购物车商品为默认选中
                cartInstance.setChecked(Const.CartCheckedCode.IS_CHECKED);
                int i = cartMapper.insert(cartInstance);
                if(i<=0) return ServerResponse.createByError("添加到购物车失败");
                else return ServerResponse.createBySuccessMessage("添加到购物车成功");
            } else {
                // 库存不足
                return ServerResponse.createByError(ResponseCode.INVENTORY_SHORTAGE.getCode(), ResponseCode.INVENTORY_SHORTAGE.getDesc(), stock);
            }
        } else {
            // 购物车中已经有此商品
            int curCount = count + cart.getQuantity();
            // 判断库存与购物车中此商品的数量关系
            if(stock >= curCount) {
                // 库存充足
                cart.setQuantity(curCount);
                int i = cartMapper.updateByPrimaryKeySelective(cart);
                if(i<=0) return ServerResponse.createByError("添加到购物车失败");
                else return ServerResponse.createBySuccessMessage("添加到购物车成功");
            } else {
                // 库存不足
                return ServerResponse.createByError(ResponseCode.INVENTORY_SHORTAGE.getCode(), ResponseCode.INVENTORY_SHORTAGE.getDesc(), stock);
            }
        }
    }

    @Override
    public ServerResponse<Integer> update(Integer userId, Integer productId, Integer count) {
        ServerResponse<Integer> errorResponse = ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        if(count == null) return errorResponse;
        Cart cart = cartMapper.selectCartByUserIdAndProductId(userId, productId);
        if(cart == null) return errorResponse;
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null) return errorResponse;
        Integer stock = product.getStock();
        int curCount = count + cart.getQuantity();
        // 判断库存与购物车中此商品的数量关系
        if(stock >= curCount) {
            // 库存充足
            cart.setQuantity(curCount);
            int i = cartMapper.updateByPrimaryKeySelective(cart);
            if(i<=0) return ServerResponse.createByError("添加到购物车失败");
            else return ServerResponse.createBySuccessMessage("添加到购物车成功");
        } else {
            // 库存不足
            return ServerResponse.createByError(ResponseCode.INVENTORY_SHORTAGE.getCode(), ResponseCode.INVENTORY_SHORTAGE.getDesc(), stock);
        }
    }

    @Override
    public ServerResponse<String> deleteProduct(Integer userId, List<Integer> cartIds) {
        if(cartIds.isEmpty()) return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        int i = cartMapper.deleteByUserIdAndIds(userId, cartIds);
        if(i == cartIds.size()) return ServerResponse.createBySuccessMessage("删除成功");
        else return ServerResponse.createByError("删除失败，部分购物车已不存在");
    }
}
