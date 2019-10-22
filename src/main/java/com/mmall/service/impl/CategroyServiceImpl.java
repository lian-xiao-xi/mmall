package com.mmall.service.impl;

import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategroyService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CategroyServiceImpl implements ICategroyService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CategroyServiceImpl.class);
    
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
    
    @Override
    public ServerResponse<List<Category>> getChildCategorys(Integer categoryId) {
        List<Category> categories = categoryMapper.selectCategorysByParentId(categoryId);
        if(categories.size() == 0) {
            LOGGER.warn("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(categories);
    }
    
    @Override
    public ServerResponse<List<Integer>> getChildDeepCategoryIds(Integer categoryId) {
        Set<Category> childDeepCategorys = this.findChildDeepCategorys(categoryId);
        List<Integer> idsList = childDeepCategorys.stream().map(Category::getId).collect(Collectors.toList());
        return ServerResponse.createBySuccess(idsList);
    }
    
    // 递归获取当前节点及其下面的所有子节点
    private Set<Category> findChildDeepCategorys(Integer categoryId) {
        HashSet<Category> set = new HashSet<>();
        // 当前节点
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if(category != null) {
            set.add(category);
        }
        // 当前节点的所有子节点
        List<Category> categories = categoryMapper.selectCategorysByParentId(categoryId);
        for(Category c: categories) {
            Set<Category> childCategory = this.findChildDeepCategorys(c.getId());
            set.addAll(childCategory);
        }
        return set;
    }
}
