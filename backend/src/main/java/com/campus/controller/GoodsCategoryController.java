package com.campus.controller;

import com.campus.common.Result;
import com.campus.pojo.GoodsCategory;
import com.campus.service.GoodsCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "商品分类接口")
@RestController
@RequestMapping("/api/category")
@CrossOrigin
public class GoodsCategoryController {

    @Autowired
    private GoodsCategoryService goodsCategoryService;

    @ApiOperation("分类列表")
    @GetMapping("/list")
    public Result<List<GoodsCategory>> getCategoryList() {
        return Result.success(goodsCategoryService.getAllCategories());
    }
}
