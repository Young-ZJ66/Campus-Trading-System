package com.campus.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.campus.common.Result;
import com.campus.pojo.PointGoods;
import com.campus.pojo.PointRecord;
import com.campus.service.PointService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.campus.pojo.PointOrder;
import com.campus.exception.GlobalException;
import com.campus.mapper.SysUserMapper;
import com.campus.pojo.SysUser;

@Api(tags = "积分商城接口")
@RestController
@RequestMapping("/api/point")
@CrossOrigin
public class PointController {

    @Autowired
    private PointService pointService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @ApiOperation("积分商品列表（用户端）")
    @GetMapping("/goods")
    public Result<List<PointGoods>> getPointGoods() {
        return Result.success(pointService.getPointGoodsList());
    }

    @ApiOperation("每日签到")
    @PostMapping("/signIn")
    public Result<Integer> signIn() {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
        int points = pointService.signIn(userId);
        return Result.success(points);
    }

    @ApiOperation("检查今日是否签到")
    @GetMapping("/checkSignIn")
    public Result<Boolean> checkSignIn() {
        if (!StpUtil.isLogin()) return Result.success(false);
        long userId = StpUtil.getLoginIdAsLong();
        return Result.success(pointService.checkTodaySignIn(userId));
    }

    @ApiOperation("获取签到日期列表")
    @GetMapping("/signInDates")
    public Result<List<String>> getSignInDates(@RequestParam("yearMonth") String yearMonth) {
        if (!StpUtil.isLogin()) return Result.success(null);
        long userId = StpUtil.getLoginIdAsLong();
        return Result.success(pointService.getSignInDates(userId, yearMonth));
    }

    @ApiOperation("积分兑换下单")
    @PostMapping("/exchange")
    public Result<Void> exchange(@RequestParam Long itemId) {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
        pointService.exchange(itemId, userId);
        return Result.success();
    }

    @ApiOperation("积分明细")
    @GetMapping("/records")
    public Result<List<PointRecord>> getRecords() {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
        return Result.success(pointService.getRecordList(userId));
    }

    @ApiOperation("积分商品详情")
    @GetMapping("/goods/{id}")
    public Result<PointGoods> getGoodsDetail(@PathVariable Long id) {
        return Result.success(pointService.getGoodsDetail(id));
    }

    @ApiOperation("积分兑换订单列表（用户端）")
    @GetMapping("/orders")
    public Result<List<PointOrder>> getOrders() {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
        return Result.success(pointService.getOrderList(userId));
    }

    @ApiOperation("核销积分订单（管理员）")
    @PostMapping("/verifyOrder")
    public Result<Void> verifyOrder(@RequestParam Long orderId) {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !"admin".equals(user.getStudentNo())) {
            throw new GlobalException("无管理员权限");
        }
        pointService.verifyOrderByAdmin(orderId);
        return Result.success();
    }
}
