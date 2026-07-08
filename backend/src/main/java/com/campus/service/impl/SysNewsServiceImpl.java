package com.campus.service.impl;

import com.campus.exception.GlobalException;
import com.campus.mapper.SysNewsMapper;
import com.campus.pojo.PageResult;
import com.campus.pojo.SysNews;
import com.campus.service.SysNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.List;

@Service
public class SysNewsServiceImpl implements SysNewsService {

    @Autowired
    private SysNewsMapper sysNewsMapper;

    @Override
    public PageResult<SysNews> getList(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<SysNews> list = sysNewsMapper.selectList(offset, pageSize);
        long total = sysNewsMapper.countList();
        return new PageResult<>(total, list);
    }

    @Override
    public SysNews getDetail(Long newsId) {
        sysNewsMapper.updateViewCount(newsId);
        return sysNewsMapper.selectById(newsId);
    }

    @Override
    public PageResult<SysNews> getAdminList(int pageNum, int pageSize, String keyword, Integer status, Integer isTop) {
        int offset = (pageNum - 1) * pageSize;
        List<SysNews> list = sysNewsMapper.selectAdminList(offset, pageSize, keyword, status, isTop);
        long total = sysNewsMapper.countAdminList(keyword, status, isTop);
        return new PageResult<>(total, list);
    }

    @Override
    public SysNews create(SysNews news) {
        if (news == null || news.getTitle() == null || news.getTitle().trim().isEmpty()) {
            throw new GlobalException("标题不能为空");
        }
        if (news.getContent() == null) {
            news.setContent("");
        }
        if (news.getIsTop() == null) {
            news.setIsTop(0);
        }
        if (news.getStatus() == null) {
            news.setStatus(1);
        }
        sysNewsMapper.insert(news);
        return news;
    }

    @Override
    public void update(SysNews news) {
        if (news == null || news.getNewsId() == null) {
            throw new GlobalException("资讯ID不能为空");
        }
        if (news.getTitle() == null || news.getTitle().trim().isEmpty()) {
            throw new GlobalException("标题不能为空");
        }
        if (news.getContent() == null) {
            news.setContent("");
        }
        if (news.getIsTop() == null) {
            news.setIsTop(0);
        }
        if (news.getStatus() == null) {
            news.setStatus(1);
        }
        int rows = sysNewsMapper.update(news);
        if (rows <= 0) {
            throw new GlobalException("更新失败");
        }
    }

    @Override
    public void delete(Long newsId) {
        if (newsId == null) {
            throw new GlobalException("资讯ID不能为空");
        }
        sysNewsMapper.deleteById(newsId);
    }

    @Override
    public void updateStatus(Long newsId, Integer status) {
        if (newsId == null || status == null) {
            throw new GlobalException("参数错误");
        }
        if (!Objects.equals(status, 0) && !Objects.equals(status, 1)) {
            throw new GlobalException("状态值错误");
        }
        sysNewsMapper.updateStatus(newsId, status);
    }

    @Override
    public void updateTop(Long newsId, Integer isTop) {
        if (newsId == null || isTop == null) {
            throw new GlobalException("参数错误");
        }
        if (!Objects.equals(isTop, 0) && !Objects.equals(isTop, 1)) {
            throw new GlobalException("置顶值错误");
        }
        sysNewsMapper.updateTop(newsId, isTop);
    }
}
