package com.campus.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GoodsInfoUpdateMapper {
    int updateStatus(@Param("goodsId") Long goodsId, @Param("status") Integer status);
}