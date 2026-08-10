package com.campus.service.impl;

import com.campus.exception.GlobalException;
import com.campus.mapper.GoodsInfoMapper;
import com.campus.pojo.GoodsInfo;
import com.campus.pojo.GoodsQueryDTO;
import com.campus.pojo.GoodsStatus;
import com.campus.pojo.PageResult;
import com.campus.service.GoodsInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.List;

@Service
public class GoodsInfoServiceImpl implements GoodsInfoService {

    @Autowired
    private GoodsInfoMapper goodsInfoMapper;

    @Override
    public void publish(GoodsInfo goodsInfo, Long userId) {
        goodsInfo.setUserId(userId);
        goodsInfo.setStatus(0); // 0-在售
        goodsInfo.setViewCount(0);
        goodsInfo.setCreateTime(LocalDateTime.now());
        
        if (goodsInfo.getIsExchange() == null) {
            goodsInfo.setIsExchange(0);
        }
        goodsInfoMapper.insert(goodsInfo);
    }

    @Override
    public PageResult<GoodsInfo> getList(GoodsQueryDTO query) {
        int pageNum = query.getSafePageNum();
        int pageSize = query.getSafePageSize();
        int offset = (pageNum - 1) * pageSize;
        List<GoodsInfo> list = goodsInfoMapper.selectList(query, offset, pageSize);
        long total = goodsInfoMapper.countList(query);
        return new PageResult<>(total, list);
    }

    @Override
    public GoodsInfo getDetail(Long goodsId) {
        GoodsInfo goodsInfo = goodsInfoMapper.selectById(goodsId);
        if (goodsInfo != null) {
            goodsInfoMapper.updateViewCount(goodsId);
        }
        return goodsInfo;
    }

    @Override
    public void updateStatus(Long goodsId, Integer status, Long userId) {
        GoodsInfo goods = goodsInfoMapper.selectById(goodsId);
        if (goods == null || !goods.getUserId().equals(userId)) {
            throw new GlobalException("商品不存在或无权操作");
        }
        if (!Objects.equals(status, GoodsStatus.ON_SALE.getValue()) && !Objects.equals(status, GoodsStatus.OFF_SHELF.getValue())) {
            throw new GlobalException("状态值错误");
        }
        if (Objects.equals(goods.getStatus(), GoodsStatus.SOLD.getValue())) {
            throw new GlobalException("已售出商品不可修改状态");
        }
        if (Objects.equals(goods.getStatus(), GoodsStatus.BARTER_PENDING.getValue())) {
            throw new GlobalException("置换锁定中商品不可修改状态");
        }
        int rows = goodsInfoMapper.updateStatusCas(goodsId, status, goods.getStatus());
        if (rows == 0) {
            throw new GlobalException("商品状态已变更，请刷新后重试");
        }
    }

    @Override
    public void updateStatusByAdmin(Long goodsId, Integer status) {
        if (goodsId == null || status == null) {
            throw new GlobalException("参数错误");
        }
        if (!Objects.equals(status, 0) && !Objects.equals(status, 2)) {
            throw new GlobalException("状态值错误");
        }
        GoodsInfo goods = goodsInfoMapper.selectById(goodsId);
        if (goods == null) {
            throw new GlobalException("商品不存在");
        }
        if (Objects.equals(goods.getStatus(), 1)) {
            throw new GlobalException("已售出商品不可修改状态");
        }
        int rows = goodsInfoMapper.updateStatusCas(goodsId, status, goods.getStatus());
        if (rows == 0) {
            throw new GlobalException("商品状态已变更，请刷新后重试");
        }
    }

    @Override
    public void updateGoods(GoodsInfo goodsInfo, Long userId) {
        GoodsInfo goods = goodsInfoMapper.selectById(goodsInfo.getGoodsId());
        if (goods == null || !goods.getUserId().equals(userId)) {
            throw new GlobalException("商品不存在或无权操作");
        }
        goodsInfoMapper.updateGoods(goodsInfo);
    }
}
