package com.mmall.service.impl;

import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategroyService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategroyServiceImpl implements ICategroyService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ServerResponse<String> addCategory(String categoryName, Integer parentId) {
        if(StringUtils.isBlank(categoryName) || parentId == null) {
            return ServerResponse.createByError("参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);
//        category.setSortOrder(0);
        int insert = categoryMapper.insert(category);
        if(insert>0) {
            return ServerResponse.createBySuccessMessage("添加品类成功");
        }
        return ServerResponse.createByError("添加品类失败");
    }

    @Override
    public ServerResponse<String> updateCategory(String categoryName, Integer categoryId) {
        if(StringUtils.isBlank(categoryName) || categoryId == null) {
            return ServerResponse.createByError("参数错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int i = categoryMapper.updateByPrimaryKeySelective(category);
        if(i>0) {
            return ServerResponse.createBySuccessMessage("更新品类成功");
        }
        return ServerResponse.createByError("更新品类失败");
    }
}
