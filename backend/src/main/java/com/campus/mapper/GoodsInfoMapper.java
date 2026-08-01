package com.campus.mapper;

import com.campus.pojo.GoodsInfo;
import com.campus.pojo.GoodsQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GoodsInfoMapper {
    int insert(GoodsInfo goodsInfo);
    
    List<GoodsInfo> selectList(@Param("query") GoodsQueryDTO query, @Param("offset") int offset, @Param("limit") int limit);
    
    long countList(@Param("query") GoodsQueryDTO query);

    long countTodayNew();

    long countOnSale();

    List<com.campus.pojo.dto.AdminDashboardTrendDTO> selectDailyNewGoods(@Param("startDate") String startDate, @Param("endDate") String endDate);
    
    GoodsInfo selectById(@Param("goodsId") Long goodsId);

    void updateViewCount(@Param("goodsId") Long goodsId);

    @Update("UPDATE goods_info SET status = #{status} WHERE goods_id = #{goodsId}")
    int updateStatus(@Param("goodsId") Long goodsId, @Param("status") Integer status);

    /**
     * 乐观锁更新商品状态（CAS），仅当期望的旧状态匹配时才更新
     * @return 影响行数，0表示状态已被其他线程修改
     */
    @Update("UPDATE goods_info SET status = #{newStatus} WHERE goods_id = #{goodsId} AND status = #{expectedStatus}")
    int updateStatusCas(@Param("goodsId") Long goodsId, @Param("newStatus") Integer newStatus, @Param("expectedStatus") Integer expectedStatus);

    @Update("<script>" +
            "UPDATE goods_info " +
            "<set>" +
            "<if test='categoryId != null'>category_id = #{categoryId},</if>" +
            "<if test='title != null and title != \"\"'>title = #{title},</if>" +
            "<if test='description != null and description != \"\"'>description = #{description},</if>" +
            "<if test='images != null and images != \"\"'>images = #{images},</if>" +
            "<if test='originalPrice != null'>original_price = #{originalPrice},</if>" +
            "<if test='price != null'>price = #{price},</if>" +
            "<if test='isExchange != null'>is_exchange = #{isExchange},</if>" +
            "<if test='exchangeDesc != null'>exchange_desc = #{exchangeDesc},</if>" +
            "</set>" +
            "WHERE goods_id = #{goodsId}" +
            "</script>")
    void updateGoods(GoodsInfo goodsInfo);
}
