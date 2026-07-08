package com.campus.service;

import com.campus.pojo.PageResult;
import com.campus.pojo.SysNews;

public interface SysNewsService {
    PageResult<SysNews> getList(int pageNum, int pageSize);
    
    SysNews getDetail(Long newsId);

    PageResult<SysNews> getAdminList(int pageNum, int pageSize, String keyword, Integer status, Integer isTop);

    SysNews create(SysNews news);

    void update(SysNews news);

    void delete(Long newsId);

    void updateStatus(Long newsId, Integer status);

    void updateTop(Long newsId, Integer isTop);
}
