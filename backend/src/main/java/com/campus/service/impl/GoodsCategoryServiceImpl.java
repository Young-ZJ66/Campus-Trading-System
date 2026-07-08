package com.campus.service.impl;

import com.campus.exception.GlobalException;
import com.campus.mapper.GoodsCategoryMapper;
import com.campus.pojo.GoodsCategory;
import com.campus.service.GoodsCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.List;

@Service
public class GoodsCategoryServiceImpl implements GoodsCategoryService {

    @Autowired
    private GoodsCategoryMapper goodsCategoryMapper;

    @Override
    public List<GoodsCategory> getAllCategories() {
        return goodsCategoryMapper.selectAllActive();
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
    }

    @Override
    public void delete(Long categoryId) {
        updateStatus(categoryId, 0);
    }
}
