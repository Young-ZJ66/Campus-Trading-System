package com.campus.mapper;

import com.campus.pojo.SysNews;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SysNewsMapper {
    List<SysNews> selectList(@Param("offset") int offset, @Param("limit") int limit);
    
    long countList();
    
    SysNews selectById(@Param("newsId") Long newsId);
    
    int updateViewCount(@Param("newsId") Long newsId);

    List<SysNews> selectAdminList(@Param("offset") int offset, @Param("limit") int limit, @Param("keyword") String keyword, @Param("status") Integer status, @Param("isTop") Integer isTop);

    long countAdminList(@Param("keyword") String keyword, @Param("status") Integer status, @Param("isTop") Integer isTop);

    int insert(SysNews sysNews);

    int update(SysNews sysNews);

    int deleteById(@Param("newsId") Long newsId);

    int updateStatus(@Param("newsId") Long newsId, @Param("status") Integer status);

    int updateTop(@Param("newsId") Long newsId, @Param("isTop") Integer isTop);
}
