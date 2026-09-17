package com.campus.service.impl;

import com.campus.exception.GlobalException;
import com.campus.mapper.GoodsCommentMapper;
import com.campus.mapper.GoodsInfoMapper;
import com.campus.pojo.GoodsComment;
import com.campus.pojo.GoodsInfo;
import com.campus.pojo.Notification;
import com.campus.service.GoodsCommentService;
import com.campus.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GoodsCommentServiceImpl implements GoodsCommentService {

    @Autowired
    private GoodsCommentMapper goodsCommentMapper;

    @Autowired
    private GoodsInfoMapper goodsInfoMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void addComment(GoodsComment comment, Long userId) {
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new GlobalException("留言内容不能为空");
        }
        comment.setUserId(userId);
        comment.setCreateTime(LocalDateTime.now());
        goodsCommentMapper.insert(comment);

        // 通知商品发布者有新评论
        try {
            GoodsInfo goods = goodsInfoMapper.selectById(comment.getGoodsId());
            if (goods != null && !goods.getUserId().equals(userId)) {
                notificationService.sendToUser(goods.getUserId(),
                        new Notification("COMMENT_ADDED", "新评论提醒",
                                "您发布的「" + goods.getTitle() + "」有新评论", comment.getGoodsId()));
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public List<GoodsComment> getCommentsByGoodsId(Long goodsId) {
        return goodsCommentMapper.selectByGoodsId(goodsId);
    }
}