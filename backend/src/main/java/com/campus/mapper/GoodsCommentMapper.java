package com.campus.mapper;

import com.campus.pojo.GoodsComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface GoodsCommentMapper {
    int insert(GoodsComment comment);
    
    List<GoodsComment> selectByGoodsId(@Param("goodsId") Long goodsId);
}