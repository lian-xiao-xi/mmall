package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShippingServiceImpl implements IShippingService {
    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public ServerResponse<Map> addShip(int userId, Shipping shipping) {
        shipping.setUserId(userId);
        int insertRow = shippingMapper.insert(shipping);
        if(insertRow>0) {
            HashMap<String, Integer> map = new HashMap<>();
            map.put("shippingId", shipping.getId());
            return ServerResponse.createBySuccess("添加地址成功", map);
        }
        return ServerResponse.createByError("添加失败");
    }

    @Override
    public ServerResponse<String> delShip(int userId, Integer shippingId) {
        int deleteRow = shippingMapper.deleteByIdAndUserId(userId, shippingId);

        // 或者下面这个写法 ---
//        Shipping shipping = shippingMapper.selectByIdAndUserId(userId, shippingId);
//        if(shipping == null) return ServerResponse.createBySuccess("此地址不是此用户的 不可删除");
//        int deleteRow = shippingMapper.deleteByPrimaryKey(shippingId);
        // -----------

        if(deleteRow>0) return ServerResponse.createBySuccess("删除地址成功");
        return ServerResponse.createBySuccess("删除地址失败");
    }

    @Override
    public ServerResponse<String> updateShip(int userId, Shipping shipping) {
        Shipping ship = shippingMapper.selectByIdAndUserId(userId, shipping.getId());
        if(ship == null) return ServerResponse.createByError("此地址不是此用户的，不可更新");
        int updateRow = shippingMapper.updateByPrimaryKeySelective(shipping);
        if(updateRow>0) return ServerResponse.createBySuccess("更新地址成功");
        return ServerResponse.createBySuccess("更新地址失败");
    }

    @Override
    public ServerResponse<Shipping> detail(int userId, Integer shippingId) {
        Shipping ship = shippingMapper.selectByIdAndUserId(userId, shippingId);
        if(ship == null) return ServerResponse.createByError("查询不到此地址");
        return ServerResponse.createBySuccess("查询地址成功", ship);
    }

    @Override
    public ServerResponse<PageInfo<Shipping>> pageList(int userId, int page, int size) {
        PageHelper.startPage(page, size);
        List<Shipping> shippings = shippingMapper.selectByUserId(userId);
        PageInfo<Shipping> shippingPageInfo = new PageInfo<>(shippings);
        return ServerResponse.createBySuccess(shippingPageInfo);
    }
}
