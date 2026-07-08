package com.campus.service.impl;

import com.campus.exception.GlobalException;
import com.campus.mapper.GoodsCommentMapper;
import com.campus.pojo.GoodsComment;
import com.campus.service.GoodsCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GoodsCommentServiceImpl implements GoodsCommentService {

    @Autowired
    private GoodsCommentMapper goodsCommentMapper;

    @Override
    public void addComment(GoodsComment comment, Long userId) {
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new GlobalException("留言内容不能为空");
        }
        comment.setUserId(userId);
        comment.setCreateTime(LocalDateTime.now());
        goodsCommentMapper.insert(comment);
    }

    @Override
    public List<GoodsComment> getCommentsByGoodsId(Long goodsId) {
        return goodsCommentMapper.selectByGoodsId(goodsId);
    }
}