package com.mmall.dao;

import com.mmall.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    List<Cart> selectCartByUserId(int userId);

    Cart selectCartByUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);

    int deleteByUserIdAndProductIds(@Param("userId") Integer userId, @Param("productIdList") List<Integer> productIdList);

    List<Cart> selectByUserIdAndIds(@Param("userId") Integer userId, @Param("cartIds") List<Integer> cartIds);

    int deleteByUserIdAndIds(@Param("userId") Integer userId, @Param("cartIdList") List<Integer> cartIdList);
}