package com.campus.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.campus.common.Result;
import com.campus.pojo.GoodsInfo;
import com.campus.service.UserFavoriteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "收藏接口")
@RestController
@RequestMapping("/api/favorite")
@CrossOrigin
public class UserFavoriteController {

    @Autowired
    private UserFavoriteService userFavoriteService;

    @ApiOperation("收藏商品")
    @PostMapping("/add")
    public Result<Void> addFavorite(@RequestParam Long goodsId) {
        StpUtil.checkLogin();
        userFavoriteService.addFavorite(StpUtil.getLoginIdAsLong(), goodsId);
        return Result.success();
    }

    @ApiOperation("取消收藏")
    @PostMapping("/remove")
    public Result<Void> removeFavorite(@RequestParam Long goodsId) {
        StpUtil.checkLogin();
        userFavoriteService.removeFavorite(StpUtil.getLoginIdAsLong(), goodsId);
        return Result.success();
    }

    @ApiOperation("检查是否收藏")
    @GetMapping("/check")
    public Result<Boolean> checkFavorite(@RequestParam Long goodsId) {
        if (!StpUtil.isLogin()) {
            return Result.success(false);
        }
        boolean isFavorite = userFavoriteService.checkFavorite(StpUtil.getLoginIdAsLong(), goodsId);
        return Result.success(isFavorite);
    }

    @ApiOperation("收藏列表")
    @GetMapping("/list")
    public Result<List<GoodsInfo>> getFavoriteList() {
        StpUtil.checkLogin();
        List<GoodsInfo> list = userFavoriteService.getFavoriteList(StpUtil.getLoginIdAsLong());
        return Result.success(list);
    }
}
