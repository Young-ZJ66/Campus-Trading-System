package com.campus.controller;

import com.campus.common.Result;
import com.campus.pojo.PageResult;
import com.campus.pojo.PointGoods;
import com.campus.pojo.PointOrder;
import com.campus.service.PointService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "管理员-积分商城管理")
@RestController
@RequestMapping("/api/admin/point")
@CrossOrigin
public class AdminPointController {

    @Autowired
    private PointService pointService;

    @ApiOperation("积分商品列表（管理员）")
    @GetMapping("/goods/list")
    public Result<PageResult<PointGoods>> goodsList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(pointService.getAdminGoodsList(pageNum, pageSize, keyword, status));
    }

    @ApiOperation("新增积分商品")
    @PostMapping("/goods/create")
    public Result<PointGoods> createGoods(@RequestBody PointGoods goods) {
        return Result.success(pointService.createGoods(goods));
    }

    @ApiOperation("编辑积分商品")
    @PostMapping("/goods/update")
    public Result<Void> updateGoods(@RequestBody PointGoods goods) {
        pointService.updateGoods(goods);
        return Result.success();
    }

    @ApiOperation("积分商品上下架")
    @PostMapping("/goods/updateStatus")
    public Result<Void> updateGoodsStatus(@RequestParam Long itemId, @RequestParam Integer status) {
        pointService.updateGoodsStatus(itemId, status);
        return Result.success();
    }

    @ApiOperation("修改积分商品库存")
    @PostMapping("/goods/updateStock")
    public Result<Void> updateGoodsStock(@RequestParam Long itemId, @RequestParam Integer stock) {
        pointService.updateGoodsStock(itemId, stock);
        return Result.success();
    }

    @ApiOperation("积分订单列表（管理员）")
    @GetMapping("/orders/list")
    public Result<PageResult<PointOrder>> orderList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(pointService.getAdminOrders(pageNum, pageSize, keyword, status));
    }

    @ApiOperation("核销积分订单（管理员）")
    @PostMapping("/orders/verify")
    public Result<Void> verifyOrder(@RequestParam Long orderId) {
        pointService.verifyOrderByAdmin(orderId);
        return Result.success();
    }
}
