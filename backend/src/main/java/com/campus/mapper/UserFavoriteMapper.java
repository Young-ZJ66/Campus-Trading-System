package com.campus.mapper;

import com.campus.pojo.GoodsInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserFavoriteMapper {

    @Insert("INSERT INTO user_favorite(user_id, goods_id) VALUES(#{userId}, #{goodsId})")
    void insert(@Param("userId") Long userId, @Param("goodsId") Long goodsId);

    @Delete("DELETE FROM user_favorite WHERE user_id = #{userId} AND goods_id = #{goodsId}")
    void delete(@Param("userId") Long userId, @Param("goodsId") Long goodsId);

    @Select("SELECT COUNT(1) FROM user_favorite WHERE user_id = #{userId} AND goods_id = #{goodsId}")
    int count(@Param("userId") Long userId, @Param("goodsId") Long goodsId);

    @Select("SELECT g.*, u.nickname AS publisherName, u.avatar AS publisherAvatar, c.name AS categoryName " +
            "FROM user_favorite f " +
            "JOIN goods_info g ON f.goods_id = g.goods_id " +
            "LEFT JOIN sys_user u ON g.user_id = u.user_id " +
            "LEFT JOIN goods_category c ON g.category_id = c.category_id " +
            "WHERE f.user_id = #{userId} " +
            "ORDER BY f.create_time DESC")
    List<GoodsInfo> selectFavoriteGoods(@Param("userId") Long userId);
}
