package com.campus.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.secure.SaSecureUtil;
import cn.hutool.crypto.SecureUtil;
import com.campus.exception.GlobalException;
import com.campus.mapper.SysUserMapper;
import com.campus.pojo.LoginDTO;
import com.campus.pojo.PageResult;
import com.campus.pojo.SysUser;
import com.campus.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public String login(LoginDTO loginDTO) {
        SysUser user = sysUserMapper.selectByStudentNo(loginDTO.getStudentNo());
        if (user == null) {
            throw new GlobalException("用户不存在");
        }
        
        // 校验密码（前端传来的明文与数据库中加密的密码进行比对）
        String encryptPassword = SaSecureUtil.md5(loginDTO.getPassword());
        if (!user.getPassword().equals(encryptPassword)) {
            throw new GlobalException("密码错误");
        }
        if (user.getStatus() == 0) {
            throw new GlobalException("账号已被禁用");
        }
        
        StpUtil.login(user.getUserId());
        return StpUtil.getTokenValue();
    }

    @Override
    public void register(SysUser sysUser) {
        SysUser existUser = sysUserMapper.selectByStudentNo(sysUser.getStudentNo());
        if (existUser != null) {
            throw new GlobalException("该学号已被注册");
        }
        
        // 密码加密存储
        sysUser.setPassword(SaSecureUtil.md5(sysUser.getPassword()));
        
        sysUser.setPoints(0);
        sysUser.setStatus(1);
        sysUser.setCreateTime(LocalDateTime.now());
        if (sysUser.getNickname() == null || sysUser.getNickname().isEmpty()) {
            sysUser.setNickname("用户_" + sysUser.getStudentNo());
        }
        
        sysUserMapper.insert(sysUser);
    }

    @Override
    public SysUser getUserInfo(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    @Override
    public void updateUser(SysUser sysUser) {
        sysUserMapper.updateUser(sysUser);
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new GlobalException("用户不存在");
        }
        String oldMd5 = SecureUtil.md5(oldPassword);
        if (!oldMd5.equals(user.getPassword())) {
            throw new GlobalException("原密码不正确");
        }
        
        SysUser updateObj = new SysUser();
        updateObj.setUserId(userId);
        updateObj.setPassword(SecureUtil.md5(newPassword));
        sysUserMapper.updateUser(updateObj);
    }

    @Override
    public PageResult<SysUser> getAdminUserList(int pageNum, int pageSize, String keyword, Integer status) {
        int offset = (pageNum - 1) * pageSize;
        List<SysUser> list = sysUserMapper.selectAdminList(offset, pageSize, keyword, status);
        list.forEach(u -> u.setPassword(null));
        long total = sysUserMapper.countAdminList(keyword, status);
        return new PageResult<>(total, list);
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        if (userId == null || status == null) {
            throw new GlobalException("参数错误");
        }
        if (!Objects.equals(status, 0) && !Objects.equals(status, 1)) {
            throw new GlobalException("状态值错误");
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new GlobalException("用户不存在");
        }
        if ("admin".equals(user.getStudentNo())) {
            throw new GlobalException("不可修改管理员状态");
        }
        sysUserMapper.updateStatus(userId, status);
    }

    @Override
    public void updateUserPoints(Long userId, Integer points) {
        if (userId == null || points == null) {
            throw new GlobalException("参数错误");
        }
        if (points < 0) {
            throw new GlobalException("积分不能为负数");
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new GlobalException("用户不存在");
        }
        if ("admin".equals(user.getStudentNo())) {
            throw new GlobalException("不可修改管理员积分");
        }
        sysUserMapper.updatePoints(userId, points);
    }
}
