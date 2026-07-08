package com.campus.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.campus.common.Result;
import com.campus.pojo.CreateOrderDTO;
import com.campus.pojo.GoodsOrder;
import com.campus.service.GoodsOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "订单接口")
@RestController
@RequestMapping("/api/order")
@CrossOrigin
public class GoodsOrderController {

    @Autowired
    private GoodsOrderService goodsOrderService;

    @ApiOperation("创建订单")
    @PostMapping("/create")
    public Result<GoodsOrder> createOrder(@RequestBody CreateOrderDTO dto) {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
        GoodsOrder order = goodsOrderService.createOrder(dto, userId);
        return Result.success(order);
    }

    @ApiOperation("查询我的买入/换入订单")
    @GetMapping("/myBuy")
    public Result<List<GoodsOrder>> getMyBuyOrders() {
        StpUtil.checkLogin();
        return Result.success(goodsOrderService.getMyBuyOrders(StpUtil.getLoginIdAsLong()));
    }

    @ApiOperation("查询我的卖出/换出订单")
    @GetMapping("/mySell")
    public Result<List<GoodsOrder>> getMySellOrders() {
        StpUtil.checkLogin();
        return Result.success(goodsOrderService.getMySellOrders(StpUtil.getLoginIdAsLong()));
    }

    @ApiOperation("处理订单（同意/拒绝/确认收货）")
    @PostMapping("/process")
    public Result<Void> processOrder(@RequestParam Long orderId, @RequestParam Integer status) {
        StpUtil.checkLogin();
        goodsOrderService.processOrder(orderId, status, StpUtil.getLoginIdAsLong());
        return Result.success();
    }

    @ApiOperation("取消订单")
    @PostMapping("/cancel")
    public Result<Void> cancelOrder(@RequestParam Long orderId) {
        StpUtil.checkLogin();
        goodsOrderService.cancelOrder(orderId, StpUtil.getLoginIdAsLong());
        return Result.success();
    }

    @ApiOperation("卖家取消订单")
    @PostMapping("/cancelBySeller")
    public Result<Void> cancelBySeller(@RequestParam Long orderId) {
        StpUtil.checkLogin();
        goodsOrderService.cancelOrderBySeller(orderId, StpUtil.getLoginIdAsLong());
        return Result.success();
    }
}
