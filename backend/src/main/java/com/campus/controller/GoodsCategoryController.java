package com.campus.controller;

import com.campus.common.Result;
import com.campus.pojo.GoodsCategory;
import com.campus.service.GoodsCategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "商品分类接口")
@RestController
@RequestMapping("/api/category")
public class GoodsCategoryController {

    @Autowired
    private GoodsCategoryService goodsCategoryService;

    @Operation(summary = "分类列表")
    @GetMapping("/list")
    public Result<List<GoodsCategory>> getCategoryList() {
        return Result.success(goodsCategoryService.getAllCategories());
    }
}
