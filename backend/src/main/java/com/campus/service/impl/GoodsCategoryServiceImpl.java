package com.campus.service.impl;

import com.campus.exception.GlobalException;
import com.campus.mapper.GoodsCategoryMapper;
import com.campus.pojo.GoodsCategory;
import com.campus.service.GoodsCategoryService;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.List;

@Service
public class GoodsCategoryServiceImpl implements GoodsCategoryService {

    @Autowired
    private GoodsCategoryMapper goodsCategoryMapper;

    @Autowired
    @Qualifier("categoryCache")
    private Cache<String, Object> categoryCache;

    private static final String CACHE_KEY_ALL = "all_active";

    @Override
    @SuppressWarnings("unchecked")
    public List<GoodsCategory> getAllCategories() {
        List<GoodsCategory> cached = (List<GoodsCategory>) categoryCache.getIfPresent(CACHE_KEY_ALL);
        if (cached != null) {
            return cached;
        }
        List<GoodsCategory> list = goodsCategoryMapper.selectAllActive();
        categoryCache.put(CACHE_KEY_ALL, list);
        return list;
    }

    @Override
    public List<GoodsCategory> getAdminList(String keyword, Integer status) {
        return goodsCategoryMapper.selectAdminList(keyword, status);
    }

    @Override
    public GoodsCategory create(GoodsCategory category) {
        if (category == null || category.getName() == null || category.getName().trim().isEmpty()) {
            throw new GlobalException("分类名称不能为空");
        }
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        goodsCategoryMapper.insert(category);
        categoryCache.invalidate(CACHE_KEY_ALL);
        return category;
    }

    @Override
    public void update(GoodsCategory category) {
        if (category == null || category.getCategoryId() == null) {
            throw new GlobalException("分类ID不能为空");
        }
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new GlobalException("分类名称不能为空");
        }
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        int rows = goodsCategoryMapper.update(category);
        if (rows <= 0) {
            throw new GlobalException("更新失败");
        }
        categoryCache.invalidate(CACHE_KEY_ALL);
    }

    @Override
    public void updateStatus(Long categoryId, Integer status) {
        if (categoryId == null || status == null) {
            throw new GlobalException("参数错误");
        }
        if (!Objects.equals(status, 0) && !Objects.equals(status, 1)) {
            throw new GlobalException("状态值错误");
        }
        GoodsCategory category = goodsCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw new GlobalException("分类不存在");
        }
        goodsCategoryMapper.updateStatus(categoryId, status);
        categoryCache.invalidate(CACHE_KEY_ALL);
    }

    @Override
    public void delete(Long categoryId) {
        updateStatus(categoryId, 0);
    }
}
