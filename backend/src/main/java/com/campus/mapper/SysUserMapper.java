package com.campus.mapper;

import com.campus.pojo.SysUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SysUserMapper {
    SysUser selectByStudentNo(@Param("studentNo") String studentNo);
    
    int insert(SysUser sysUser);
    
    SysUser selectById(@Param("userId") Long userId);

    List<SysUser> selectAdminList(@Param("offset") int offset, @Param("limit") int limit, @Param("keyword") String keyword, @Param("status") Integer status);

    long countAdminList(@Param("keyword") String keyword, @Param("status") Integer status);

    int updateStatus(@Param("userId") Long userId, @Param("status") Integer status);

    int updatePoints(@Param("userId") Long userId, @Param("points") Integer points);

    long countTodayNew();

    List<com.campus.pojo.dto.AdminDashboardTrendDTO> selectDailyNewUsers(@Param("startDate") String startDate, @Param("endDate") String endDate);

    @Update("<script>" +
            "UPDATE sys_user " +
            "<set>" +
            "<if test='nickname != null and nickname != \"\"'>nickname = #{nickname},</if>" +
            "<if test='phone != null and phone != \"\"'>phone = #{phone},</if>" +
            "<if test='avatar != null and avatar != \"\"'>avatar = #{avatar},</if>" +
            "<if test='password != null and password != \"\"'>password = #{password},</if>" +
            "</set>" +
            "WHERE user_id = #{userId}" +
            "</script>")
    void updateUser(SysUser sysUser);
}
