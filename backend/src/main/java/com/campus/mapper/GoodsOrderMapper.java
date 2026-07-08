package com.campus.mapper;

import com.campus.pojo.GoodsOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.math.BigDecimal;

@Mapper
public interface GoodsOrderMapper {
    int insert(GoodsOrder goodsOrder);
    
    GoodsOrder selectById(@Param("orderId") Long orderId);
    
    List<GoodsOrder> selectByUserId(@Param("userId") Long userId, @Param("type") String type); // type: "buy" or "sell"
    
    int updateStatus(@Param("orderId") Long orderId, @Param("status") Integer status);

    List<GoodsOrder> selectPendingExchangeOrdersByGoodsId(@Param("goodsId") Long goodsId);

    List<GoodsOrder> selectPendingExchangeOrdersByGoodsIdExcludeOrderId(@Param("goodsId") Long goodsId, @Param("excludeOrderId") Long excludeOrderId);

    int cancelPendingExchangeOrdersByGoodsId(@Param("goodsId") Long goodsId);

    int cancelPendingExchangeOrdersByGoodsIdExcludeOrderId(@Param("goodsId") Long goodsId, @Param("excludeOrderId") Long excludeOrderId);

    List<GoodsOrder> selectAdminList(@Param("offset") int offset, @Param("limit") int limit, @Param("keyword") String keyword, @Param("tradeType") Integer tradeType, @Param("status") Integer status);

    long countAdminList(@Param("keyword") String keyword, @Param("tradeType") Integer tradeType, @Param("status") Integer status);

    long countTodayNew();

    long countCompletedTotal();

    long countCompletedToday();

    BigDecimal sumGmvTotal();

    BigDecimal sumGmvToday();

    List<com.campus.pojo.dto.AdminDashboardTrendDTO> selectOrderTrend(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
