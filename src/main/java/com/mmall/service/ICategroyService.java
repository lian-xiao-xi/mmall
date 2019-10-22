package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;

import java.util.List;

public interface ICategroyService {
    ServerResponse<String> addCategory(String categoryName, Integer parentId);
    ServerResponse<String> updateCategory(String categoryName, Integer categoryId);
    ServerResponse<List<Category>> getChildCategorys(Integer categoryId);
    ServerResponse<List<Integer>> getChildDeepCategoryIds(Integer categoryId);
}
