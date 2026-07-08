package com.campus.mapper;

import com.campus.pojo.GoodsCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface GoodsCategoryMapper {
    List<GoodsCategory> selectAllActive();

    List<GoodsCategory> selectAdminList(@Param("keyword") String keyword, @Param("status") Integer status);

    GoodsCategory selectById(@Param("categoryId") Long categoryId);

    int insert(GoodsCategory category);

    int update(GoodsCategory category);

    int updateStatus(@Param("categoryId") Long categoryId, @Param("status") Integer status);
}
