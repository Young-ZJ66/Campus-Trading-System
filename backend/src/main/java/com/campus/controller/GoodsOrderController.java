package com.campus.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.campus.common.Result;
import com.campus.pojo.CreateOrderDTO;
import com.campus.pojo.GoodsOrder;
import com.campus.service.GoodsOrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "订单接口")
@RestController
@RequestMapping("/api/order")
public class GoodsOrderController {

    @Autowired
    private GoodsOrderService goodsOrderService;

    @Operation(summary = "创建订单")
    @PostMapping("/create")
    public Result<GoodsOrder> createOrder(@RequestBody CreateOrderDTO dto) {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
        GoodsOrder order = goodsOrderService.createOrder(dto, userId);
        return Result.success(order);
    }

    @Operation(summary = "查询我的买入/换入订单")
    @GetMapping("/myBuy")
    public Result<List<GoodsOrder>> getMyBuyOrders() {
        StpUtil.checkLogin();
        return Result.success(goodsOrderService.getMyBuyOrders(StpUtil.getLoginIdAsLong()));
    }

    @Operation(summary = "查询我的卖出/换出订单")
    @GetMapping("/mySell")
    public Result<List<GoodsOrder>> getMySellOrders() {
        StpUtil.checkLogin();
        return Result.success(goodsOrderService.getMySellOrders(StpUtil.getLoginIdAsLong()));
    }

    @Operation(summary = "处理订单（同意/拒绝/确认收货）")
    @PostMapping("/process")
    public Result<Void> processOrder(@RequestParam Long orderId, @RequestParam Integer status) {
        StpUtil.checkLogin();
        goodsOrderService.processOrder(orderId, status, StpUtil.getLoginIdAsLong());
        return Result.success();
    }

    @Operation(summary = "取消订单")
    @PostMapping("/cancel")
    public Result<Void> cancelOrder(@RequestParam Long orderId) {
        StpUtil.checkLogin();
        goodsOrderService.cancelOrder(orderId, StpUtil.getLoginIdAsLong());
        return Result.success();
    }

    @Operation(summary = "卖家取消订单")
    @PostMapping("/cancelBySeller")
    public Result<Void> cancelBySeller(@RequestParam Long orderId) {
        StpUtil.checkLogin();
        goodsOrderService.cancelOrderBySeller(orderId, StpUtil.getLoginIdAsLong());
        return Result.success();
    }
}
