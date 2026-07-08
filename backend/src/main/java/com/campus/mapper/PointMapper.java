package com.campus.mapper;

import com.campus.pojo.PointGoods;
import com.campus.pojo.PointRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

import com.campus.pojo.PointOrder;

@Mapper
public interface PointMapper {
    // 积分商品相关
    List<PointGoods> selectPointGoods();
    PointGoods selectPointGoodsById(@Param("itemId") Long itemId);
    int updateStock(@Param("itemId") Long itemId);

    List<PointGoods> selectAdminPointGoods(@Param("keyword") String keyword, @Param("status") Integer status, @Param("offset") int offset, @Param("limit") int limit);

    long countAdminPointGoods(@Param("keyword") String keyword, @Param("status") Integer status);

    int insertPointGoods(PointGoods goods);

    int updatePointGoods(PointGoods goods);

    int updatePointGoodsStatus(@Param("itemId") Long itemId, @Param("status") Integer status);

    int updatePointGoodsStock(@Param("itemId") Long itemId, @Param("stock") Integer stock);
    
    // 用户积分相关
    int updateUserPoints(@Param("userId") Long userId, @Param("points") Integer points);
    int insertRecord(PointRecord record);
    List<PointRecord> selectRecordList(@Param("userId") Long userId);
    
    // 检查今日是否已签到
    int checkTodaySignIn(@Param("userId") Long userId);
    List<String> selectSignInDates(@Param("userId") Long userId, @Param("yearMonth") String yearMonth);

    // 兑换订单相关
    int insertOrder(PointOrder order);
    List<PointOrder> selectOrdersByUserId(@Param("userId") Long userId);
    int verifyOrder(@Param("orderId") Long orderId);

    List<PointOrder> selectAdminOrders(@Param("keyword") String keyword, @Param("status") Integer status, @Param("offset") int offset, @Param("limit") int limit);

    long countAdminOrders(@Param("keyword") String keyword, @Param("status") Integer status);

    long countTodayOrders();
}
