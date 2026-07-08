package com.campus.service.impl;

import com.campus.mapper.UserFavoriteMapper;
import com.campus.pojo.GoodsInfo;
import com.campus.service.UserFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserFavoriteServiceImpl implements UserFavoriteService {

    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    @Override
    public void addFavorite(Long userId, Long goodsId) {
        if (!checkFavorite(userId, goodsId)) {
            userFavoriteMapper.insert(userId, goodsId);
        }
    }

    @Override
    public void removeFavorite(Long userId, Long goodsId) {
        userFavoriteMapper.delete(userId, goodsId);
    }

    @Override
    public boolean checkFavorite(Long userId, Long goodsId) {
        return userFavoriteMapper.count(userId, goodsId) > 0;
    }

    @Override
    public List<GoodsInfo> getFavoriteList(Long userId) {
        return userFavoriteMapper.selectFavoriteGoods(userId);
    }
}
