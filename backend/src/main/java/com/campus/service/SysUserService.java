package com.campus.service;

import com.campus.pojo.LoginDTO;
import com.campus.pojo.PageResult;
import com.campus.pojo.SysUser;
import com.campus.pojo.dto.UpdateUserDTO;

public interface SysUserService {
    String login(LoginDTO loginDTO);
    void register(SysUser sysUser);

    SysUser getUserInfo(Long userId);
    
    void updateUser(Long userId, UpdateUserDTO dto);

    void updatePassword(Long userId, String oldPassword, String newPassword);

    PageResult<SysUser> getAdminUserList(int pageNum, int pageSize, String keyword, Integer status);

    void updateUserStatus(Long userId, Integer status);

    void updateUserPoints(Long userId, Integer points);
}
