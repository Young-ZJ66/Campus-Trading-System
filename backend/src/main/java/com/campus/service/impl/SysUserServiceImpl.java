package com.campus.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.campus.common.LoginAttemptLimiter;
import com.campus.common.PageQuery;
import com.campus.exception.GlobalException;
import com.campus.mapper.SysUserMapper;
import com.campus.pojo.LoginDTO;
import com.campus.pojo.PageResult;
import com.campus.pojo.SysUser;
import com.campus.pojo.dto.UpdateUserDTO;
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

    @Autowired
    private LoginAttemptLimiter loginAttemptLimiter;

    @Override
    public String login(LoginDTO loginDTO) {
        loginAttemptLimiter.checkAllowed(loginDTO.getStudentNo());
        SysUser user = sysUserMapper.selectByStudentNo(loginDTO.getStudentNo());
        if (user == null) {
            throw new GlobalException("用户不存在");
        }

        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            loginAttemptLimiter.recordFailure(loginDTO.getStudentNo());
            throw new GlobalException("密码错误");
        }
        loginAttemptLimiter.recordSuccess(loginDTO.getStudentNo());
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

        sysUser.setPassword(BCrypt.hashpw(sysUser.getPassword(), BCrypt.gensalt()));

        sysUser.setPoints(0);
        sysUser.setStatus(1);
        sysUser.setRole(0); // 注册用户默认为普通用户，防止越权注册管理员
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
    public void updateUser(Long userId, UpdateUserDTO dto) {
        SysUser updateObj = new SysUser();
        updateObj.setUserId(userId);
        updateObj.setNickname(dto.getNickname());
        updateObj.setPhone(dto.getPhone());
        updateObj.setAvatar(dto.getAvatar());
        sysUserMapper.updateUser(updateObj);
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new GlobalException("用户不存在");
        }

        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new GlobalException("原密码不正确");
        }

        SysUser updateObj = new SysUser();
        updateObj.setUserId(userId);
        updateObj.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        sysUserMapper.updateUser(updateObj);
    }

    @Override
    public PageResult<SysUser> getAdminUserList(int pageNum, int pageSize, String keyword, Integer status) {
        int page = PageQuery.normalizePageNum(pageNum);
        int size = PageQuery.normalizePageSize(pageSize);
        int offset = PageQuery.offset(page, size);
        List<SysUser> list = sysUserMapper.selectAdminList(offset, size, keyword, status);
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
        if (isAdminRole(user)) {
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
        if (isAdminRole(user)) {
            throw new GlobalException("不可修改管理员积分");
        }
        sysUserMapper.updatePoints(userId, points);
    }

    /**
     * 基于 role 字段判断是否为管理员
     */
    private boolean isAdminRole(SysUser user) {
        return Objects.equals(user.getRole(), 1);
    }
}