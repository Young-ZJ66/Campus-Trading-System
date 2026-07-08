package com.campus.service;

import com.campus.pojo.GoodsInfo;
import com.campus.pojo.GoodsQueryDTO;
import com.campus.pojo.PageResult;

public interface GoodsInfoService {
    void publish(GoodsInfo goodsInfo, Long userId);
    
    PageResult<GoodsInfo> getList(GoodsQueryDTO query);
    
    GoodsInfo getDetail(Long goodsId);

    void updateStatus(Long goodsId, Integer status, Long userId);

    void updateStatusByAdmin(Long goodsId, Integer status);

    void updateGoods(GoodsInfo goodsInfo, Long userId);
}
