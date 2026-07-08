package com.campus.service;

import com.campus.pojo.GoodsCategory;
import java.util.List;

public interface GoodsCategoryService {
    List<GoodsCategory> getAllCategories();

    List<GoodsCategory> getAdminList(String keyword, Integer status);

    GoodsCategory create(GoodsCategory category);

    void update(GoodsCategory category);

    void updateStatus(Long categoryId, Integer status);

    void delete(Long categoryId);
}
