package com.campus.controller;

import com.campus.common.Result;
import com.campus.pojo.GoodsOrder;
import com.campus.pojo.PageResult;
import com.campus.service.GoodsOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "管理员-订单管理")
@RestController
@RequestMapping("/api/admin/order")
@CrossOrigin
public class AdminOrderController {

    @Autowired
    private GoodsOrderService goodsOrderService;

    @ApiOperation("订单列表（管理员）")
    @GetMapping("/list")
    public Result<PageResult<GoodsOrder>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer tradeType,
            @RequestParam(required = false) Integer status) {
        return Result.success(goodsOrderService.getAdminOrderList(pageNum, pageSize, keyword, tradeType, status));
    }

    @ApiOperation("订单详情（管理员）")
    @GetMapping("/detail/{id}")
    public Result<GoodsOrder> detail(@PathVariable Long id) {
        return Result.success(goodsOrderService.getAdminOrderDetail(id));
    }

    @ApiOperation("强制取消订单（管理员）")
    @PostMapping("/forceCancel")
    public Result<Void> forceCancel(@RequestParam Long orderId) {
        goodsOrderService.forceCancelByAdmin(orderId);
        return Result.success();
    }

    @ApiOperation("强制完成订单（管理员）")
    @PostMapping("/forceComplete")
    public Result<Void> forceComplete(@RequestParam Long orderId) {
        goodsOrderService.forceCompleteByAdmin(orderId);
        return Result.success();
    }
}
