package com.mmall.service;

import com.mmall.common.ServerResponse;

public interface ICategroyService {
    ServerResponse<String> addCategory(String categoryName, Integer parentId);
    ServerResponse<String> updateCategory(String categoryName, Integer categoryId);
}
