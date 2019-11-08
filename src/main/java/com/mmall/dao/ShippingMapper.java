package com.mmall.dao;

import com.mmall.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);

    Shipping selectByIdAndUserId(@Param("id") Integer id, @Param("userId") Integer userId);

    int deleteByIdAndUserId(@Param("id") Integer id, @Param("userId") Integer userId);

    List<Shipping> selectByUserId(Integer userId);
}