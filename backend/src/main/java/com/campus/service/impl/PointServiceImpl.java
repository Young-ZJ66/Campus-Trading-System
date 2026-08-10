package com.campus.service.impl;

import com.campus.exception.GlobalException;
import com.campus.mapper.PointMapper;
import com.campus.mapper.SysUserMapper;
import com.campus.pojo.PointGoods;
import com.campus.pojo.PageResult;
import com.campus.pojo.PointRecord;
import com.campus.pojo.SysUser;
import com.campus.service.PointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import java.util.Random;

import com.campus.pojo.PointOrder;
import cn.hutool.core.util.IdUtil;

@Service
public class PointServiceImpl implements PointService {

    @Autowired
    private PointMapper pointMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public List<PointGoods> getPointGoodsList() {
        return pointMapper.selectPointGoods();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int signIn(Long userId) {
               if (pointMapper.checkTodaySignIn(userId) > 0) {
            throw new GlobalException("今日已签到");
        }
        
        int signPoints = new Random().nextInt(10) + 1; // 1-10 points
        pointMapper.updateUserPoints(userId, signPoints);
        
        SysUser user = sysUserMapper.selectById(userId);
        
        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setChangeType(0); // 0-签到获取
        record.setChangeAmount(signPoints);
        record.setBalanceAfter(user.getPoints());
        record.setCreateTime(LocalDateTime.now());
        pointMapper.insertRecord(record);
        
        return signPoints;
    }

    @Override
    public boolean checkTodaySignIn(Long userId) {
        return pointMapper.checkTodaySignIn(userId) > 0;
    }

    @Override
    public List<String> getSignInDates(Long userId, String yearMonth) {
        return pointMapper.selectSignInDates(userId, yearMonth + "%");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exchange(Long itemId, Long userId) {
        PointGoods goods = pointMapper.selectPointGoodsById(itemId);
        if (goods == null || goods.getStatus() == 0) {
            throw new GlobalException("该商品已下架");
        }
        if (goods.getStock() <= 0) {
            throw new GlobalException("库存不足");
        }

SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new GlobalException("用户不存在");
        }
        if (user.getPoints() < goods.getPointsRequired()) {
            throw new GlobalException("您的积分不足");
        }

        int updateStock = pointMapper.updateStock(itemId);
        if (updateStock == 0) {
            throw new GlobalException("商品已被抢光");
        }

        // 原子扣减积分，防止并发超扣导致负余额
        int deductRows = pointMapper.deductUserPoints(userId, goods.getPointsRequired());
        if (deductRows == 0) {
            throw new GlobalException("您的积分不足");
        }

        SysUser freshUser = sysUserMapper.selectById(userId);

        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setChangeType(2); // 2-兑换消耗
        record.setChangeAmount(-goods.getPointsRequired());
        record.setBalanceAfter(freshUser.getPoints());
        record.setCreateTime(LocalDateTime.now());
        pointMapper.insertRecord(record);

        PointOrder order = new PointOrder();
        order.setOrderNo("PT" + IdUtil.getSnowflakeNextIdStr());
        order.setUserId(userId);
        order.setItemId(itemId);
        order.setPointsUsed(goods.getPointsRequired());
        order.setStatus(0); // 待核销
        pointMapper.insertOrder(order);
    }

    @Override
    public List<PointRecord> getRecordList(Long userId) {
        return pointMapper.selectRecordList(userId);
    }

    @Override
    public List<PointOrder> getOrderList(Long userId) {
        return pointMapper.selectOrdersByUserId(userId);
    }

    @Override
    public void verifyOrder(Long orderId) {
        pointMapper.verifyOrder(orderId);
    }

    @Override
    public PointGoods getGoodsDetail(Long itemId) {
        return pointMapper.selectPointGoodsById(itemId);
    }

    @Override
    public PageResult<PointGoods> getAdminGoodsList(int pageNum, int pageSize, String keyword, Integer status) {
        // 分页参数边界校验
        pageNum = Math.max(1, pageNum);
        pageSize = Math.min(100, Math.max(1, pageSize));
        int offset = (pageNum - 1) * pageSize;
        List<PointGoods> list = pointMapper.selectAdminPointGoods(keyword, status, offset, pageSize);
        long total = pointMapper.countAdminPointGoods(keyword, status);
        return new PageResult<>(total, list);
    }

    @Override
    public PointGoods createGoods(PointGoods goods) {
        if (goods == null || goods.getName() == null || goods.getName().trim().isEmpty()) {
            throw new GlobalException("商品名称不能为空");
        }
        if (goods.getPointsRequired() == null || goods.getPointsRequired() < 0) {
            throw new GlobalException("所需积分不合法");
        }
        if (goods.getStock() == null || goods.getStock() < 0) {
            throw new GlobalException("库存不合法");
        }
        if (goods.getStatus() == null) {
            goods.setStatus(1);
        }
        if (!Objects.equals(goods.getStatus(), 0) && !Objects.equals(goods.getStatus(), 1)) {
            throw new GlobalException("状态值错误");
        }
        pointMapper.insertPointGoods(goods);
        return goods;
    }

    @Override
    public void updateGoods(PointGoods goods) {
        if (goods == null || goods.getItemId() == null) {
            throw new GlobalException("商品ID不能为空");
        }
        if (goods.getName() == null || goods.getName().trim().isEmpty()) {
            throw new GlobalException("商品名称不能为空");
        }
        if (goods.getPointsRequired() == null || goods.getPointsRequired() < 0) {
            throw new GlobalException("所需积分不合法");
        }
        if (goods.getStock() == null || goods.getStock() < 0) {
            throw new GlobalException("库存不合法");
        }
        if (goods.getStatus() == null) {
            goods.setStatus(1);
        }
        if (!Objects.equals(goods.getStatus(), 0) && !Objects.equals(goods.getStatus(), 1)) {
            throw new GlobalException("状态值错误");
        }
        int rows = pointMapper.updatePointGoods(goods);
        if (rows <= 0) {
            throw new GlobalException("更新失败");
        }
    }

    @Override
    public void updateGoodsStatus(Long itemId, Integer status) {
        if (itemId == null || status == null) {
            throw new GlobalException("参数错误");
        }
        if (!Objects.equals(status, 0) && !Objects.equals(status, 1)) {
            throw new GlobalException("状态值错误");
        }
        pointMapper.updatePointGoodsStatus(itemId, status);
    }

    @Override
    public void updateGoodsStock(Long itemId, Integer stock) {
        if (itemId == null || stock == null || stock < 0) {
            throw new GlobalException("库存不合法");
        }
        pointMapper.updatePointGoodsStock(itemId, stock);
    }

    @Override
    public PageResult<PointOrder> getAdminOrders(int pageNum, int pageSize, String keyword, Integer status) {
        // 分页参数边界校验
        pageNum = Math.max(1, pageNum);
        pageSize = Math.min(100, Math.max(1, pageSize));
        int offset = (pageNum - 1) * pageSize;
        List<PointOrder> list = pointMapper.selectAdminOrders(keyword, status, offset, pageSize);
        long total = pointMapper.countAdminOrders(keyword, status);
        return new PageResult<>(total, list);
    }

    @Override
    public void verifyOrderByAdmin(Long orderId) {
        if (orderId == null) {
            throw new GlobalException("订单ID不能为空");
        }
        pointMapper.verifyOrder(orderId);
    }
}
