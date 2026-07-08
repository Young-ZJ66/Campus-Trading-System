package com.campus.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.campus.common.Result;
import com.campus.pojo.GoodsInfo;
import com.campus.pojo.GoodsQueryDTO;
import com.campus.pojo.PageResult;
import com.campus.service.GoodsInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "商品接口")
@RestController
@RequestMapping("/api/goods")
@CrossOrigin
public class GoodsInfoController {

    @Autowired
    private GoodsInfoService goodsInfoService;

    @ApiOperation("发布商品")
    @PostMapping("/publish")
    public Result<Void> publish(@RequestBody GoodsInfo goodsInfo) {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
        goodsInfoService.publish(goodsInfo, userId);
        return Result.success();
    }

    @ApiOperation("更新商品")
    @PostMapping("/update")
    public Result<Void> update(@RequestBody GoodsInfo goodsInfo) {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
        goodsInfoService.updateGoods(goodsInfo, userId);
        return Result.success();
    }

    @ApiOperation("商品列表")
    @PostMapping("/list")
    public Result<PageResult<GoodsInfo>> getList(@RequestBody GoodsQueryDTO query) {
        return Result.success(goodsInfoService.getList(query));
    }

    @ApiOperation("查询我发布的商品")
    @GetMapping("/myPublished")
    public Result<List<GoodsInfo>> getMyPublished() {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
        GoodsQueryDTO queryDTO = new GoodsQueryDTO();
        queryDTO.setUserId(userId);
        queryDTO.setStatus(-1); // 查询所有状态的发布商品
        queryDTO.setPageSize(100); // 获取全部
        PageResult<GoodsInfo> pageResult = goodsInfoService.getList(queryDTO);
        return Result.success(pageResult.getList());
    }

    @ApiOperation("商品详情")
    @GetMapping("/detail/{id}")
    public Result<GoodsInfo> getDetail(@PathVariable Long id) {
        GoodsInfo goodsInfo = goodsInfoService.getDetail(id);
        return Result.success(goodsInfo);
    }

    @ApiOperation("更新商品状态（上架/下架）")
    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@RequestParam Long goodsId, @RequestParam Integer status) {
        StpUtil.checkLogin();
        goodsInfoService.updateStatus(goodsId, status, StpUtil.getLoginIdAsLong());
        return Result.success();
    }
}
