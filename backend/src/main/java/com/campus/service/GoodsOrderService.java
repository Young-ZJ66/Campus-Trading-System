package com.campus.service;

import com.campus.pojo.CreateOrderDTO;
import com.campus.pojo.GoodsOrder;
import com.campus.pojo.PageResult;
import java.util.List;

public interface GoodsOrderService {
    GoodsOrder createOrder(CreateOrderDTO dto, Long userId);
    
    List<GoodsOrder> getMyBuyOrders(Long userId);
    
    List<GoodsOrder> getMySellOrders(Long userId);
    
    void processOrder(Long orderId, Integer status, Long userId);

    void cancelOrder(Long orderId, Long userId);

    void cancelOrderBySeller(Long orderId, Long sellerId);

    PageResult<GoodsOrder> getAdminOrderList(int pageNum, int pageSize, String keyword, Integer tradeType, Integer status);

    GoodsOrder getAdminOrderDetail(Long orderId);

    void forceCancelByAdmin(Long orderId);

    void forceCompleteByAdmin(Long orderId);
}
