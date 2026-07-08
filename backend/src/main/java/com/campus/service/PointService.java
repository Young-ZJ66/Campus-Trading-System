package com.campus.service;

import com.campus.pojo.PointGoods;
import com.campus.pojo.PointRecord;
import com.campus.pojo.PointOrder;
import com.campus.pojo.PageResult;

import java.util.List;

public interface PointService {
    List<PointGoods> getPointGoodsList();
    
    int signIn(Long userId);
    boolean checkTodaySignIn(Long userId);
    List<String> getSignInDates(Long userId, String yearMonth);
    
    void exchange(Long itemId, Long userId);

    List<PointRecord> getRecordList(Long userId);

    List<PointOrder> getOrderList(Long userId);
    void verifyOrder(Long orderId);
    PointGoods getGoodsDetail(Long itemId);

    PageResult<PointGoods> getAdminGoodsList(int pageNum, int pageSize, String keyword, Integer status);

    PointGoods createGoods(PointGoods goods);

    void updateGoods(PointGoods goods);

    void updateGoodsStatus(Long itemId, Integer status);

    void updateGoodsStock(Long itemId, Integer stock);

    PageResult<PointOrder> getAdminOrders(int pageNum, int pageSize, String keyword, Integer status);

    void verifyOrderByAdmin(Long orderId);
}
