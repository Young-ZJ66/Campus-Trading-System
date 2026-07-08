package com.campus.service;

import com.campus.pojo.GoodsComment;
import java.util.List;

public interface GoodsCommentService {
    void addComment(GoodsComment comment, Long userId);
    
    List<GoodsComment> getCommentsByGoodsId(Long goodsId);
}