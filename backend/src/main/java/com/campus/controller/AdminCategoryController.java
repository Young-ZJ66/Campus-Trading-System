package com.campus.controller;

import com.campus.common.Result;
import com.campus.pojo.GoodsCategory;
import com.campus.service.GoodsCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "管理员-分类管理")
@RestController
@RequestMapping("/api/admin/category")
@CrossOrigin
public class AdminCategoryController {

    @Autowired
    private GoodsCategoryService goodsCategoryService;

    @ApiOperation("分类列表（管理员）")
    @GetMapping("/list")
    public Result<List<GoodsCategory>> list(@RequestParam(required = false) String keyword, @RequestParam(required = false) Integer status) {
        return Result.success(goodsCategoryService.getAdminList(keyword, status));
    }

    @ApiOperation("新增分类")
    @PostMapping("/create")
    public Result<GoodsCategory> create(@RequestBody GoodsCategory category) {
        return Result.success(goodsCategoryService.create(category));
    }

    @ApiOperation("编辑分类")
    @PostMapping("/update")
    public Result<Void> update(@RequestBody GoodsCategory category) {
        goodsCategoryService.update(category);
        return Result.success();
    }

    @ApiOperation("分类启用/禁用")
    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@RequestParam Long categoryId, @RequestParam Integer status) {
        goodsCategoryService.updateStatus(categoryId, status);
        return Result.success();
    }

    @ApiOperation("删除分类（软删除）")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long categoryId) {
        goodsCategoryService.delete(categoryId);
        return Result.success();
    }
}
