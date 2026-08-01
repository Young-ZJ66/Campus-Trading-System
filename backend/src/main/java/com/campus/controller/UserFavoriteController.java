package com.campus.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.campus.common.Result;
import com.campus.pojo.GoodsInfo;
import com.campus.service.UserFavoriteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "收藏接口")
@RestController
@RequestMapping("/api/favorite")
public class UserFavoriteController {

    @Autowired
    private UserFavoriteService userFavoriteService;

    @Operation(summary = "收藏商品")
    @PostMapping("/add")
    public Result<Void> addFavorite(@RequestParam Long goodsId) {
        StpUtil.checkLogin();
        userFavoriteService.addFavorite(StpUtil.getLoginIdAsLong(), goodsId);
        return Result.success();
    }

    @Operation(summary = "取消收藏")
    @PostMapping("/remove")
    public Result<Void> removeFavorite(@RequestParam Long goodsId) {
        StpUtil.checkLogin();
        userFavoriteService.removeFavorite(StpUtil.getLoginIdAsLong(), goodsId);
        return Result.success();
    }

    @Operation(summary = "检查是否收藏")
    @GetMapping("/check")
    public Result<Boolean> checkFavorite(@RequestParam Long goodsId) {
        if (!StpUtil.isLogin()) {
            return Result.success(false);
        }
        boolean isFavorite = userFavoriteService.checkFavorite(StpUtil.getLoginIdAsLong(), goodsId);
        return Result.success(isFavorite);
    }

    @Operation(summary = "收藏列表")
    @GetMapping("/list")
    public Result<List<GoodsInfo>> getFavoriteList() {
        StpUtil.checkLogin();
        List<GoodsInfo> list = userFavoriteService.getFavoriteList(StpUtil.getLoginIdAsLong());
        return Result.success(list);
    }
}
