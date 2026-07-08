package com.campus.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.campus.common.Result;
import com.campus.pojo.LoginDTO;
import com.campus.pojo.SysUser;
import com.campus.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "用户接口")
@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody LoginDTO loginDTO) {
        String token = sysUserService.login(loginDTO);
        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        return Result.success(map);
    }

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody SysUser sysUser) {
        sysUserService.register(sysUser);
        return Result.success();
    }

    @ApiOperation("获取用户信息")
    @GetMapping("/info")
    public Result<SysUser> getUserInfo() {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
        SysUser userInfo = sysUserService.getUserInfo(userId);
        return Result.success(userInfo);
    }

    @ApiOperation("更新用户信息")
    @PostMapping("/update")
    public Result<Void> updateUser(@RequestBody SysUser sysUser) {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
        sysUser.setUserId(userId);
        sysUserService.updateUser(sysUser);
        return Result.success();
    }

    @ApiOperation("修改密码")
    @PostMapping("/updatePassword")
    public Result<Void> updatePassword(@RequestBody com.campus.pojo.dto.UpdatePasswordDTO dto) {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
        sysUserService.updatePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }

    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
        return Result.success();
    }
}
