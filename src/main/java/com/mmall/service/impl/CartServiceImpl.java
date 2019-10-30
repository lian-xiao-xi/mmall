package com.mmall.service.impl;

import com.mmall.common.BigDecimalUtil;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.CartService;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
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
                // 在添加商品到购物车或者修改购物车商品数量是需要判断库存与购物车中此商品的数量关系，如果前者较小，则需要将购物车中此商品的数量设置为前者
//                if(cart.getQuantity() <= product.getStock()) {
//
//                } else {
//
//                }
                BigDecimal totalPrice = BigDecimalUtil.add(product.getPrice().doubleValue(), cart.getQuantity());
                cartProductVo.setProductTotalPrice(totalPrice);
            }
            return cartProductVo;
        }).collect(Collectors.toList());
        CartVo cartVo = new CartVo();
        cartVo.setCartProductVoList(cartProductVoList);

        return ServerResponse.createBySuccess(cartVo);
    }
}
