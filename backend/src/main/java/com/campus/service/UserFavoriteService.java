package com.campus.service;

import com.campus.pojo.GoodsInfo;
import java.util.List;

public interface UserFavoriteService {
    void addFavorite(Long userId, Long goodsId);
    void removeFavorite(Long userId, Long goodsId);
    boolean checkFavorite(Long userId, Long goodsId);
    List<GoodsInfo> getFavoriteList(Long userId);
}
