package com.campus.config;

import cn.dev33.satoken.stp.StpInterface;
import com.campus.mapper.SysUserMapper;
import com.campus.pojo.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义权限加载接口实现类，用于 Sa-Token 角色/权限鉴权
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> roles = new ArrayList<>();
        if (loginId == null) {
            return roles;
        }
        try {
            long userId = Long.parseLong(loginId.toString());
            SysUser user = sysUserMapper.selectById(userId);
            if (user != null) {
                if (user.getRole() != null && user.getRole() == 1) {
                    roles.add("admin");
                } else {
                    roles.add("user");
                }
            }
        } catch (NumberFormatException e) {
        }
        return roles;
    }
}
