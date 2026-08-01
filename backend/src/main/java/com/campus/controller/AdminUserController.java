package com.campus.controller;

import com.campus.common.Result;
import com.campus.pojo.PageResult;
import com.campus.pojo.SysUser;
import com.campus.service.SysUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员-用户管理")
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    @Autowired
    private SysUserService sysUserService;

    @Operation(summary = "用户列表（管理员）")
    @GetMapping("/list")
    public Result<PageResult<SysUser>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(sysUserService.getAdminUserList(pageNum, pageSize, keyword, status));
    }

    @Operation(summary = "用户启用/禁用（管理员）")
    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@RequestParam Long userId, @RequestParam Integer status) {
        sysUserService.updateUserStatus(userId, status);
        return Result.success();
    }

    @Operation(summary = "调整用户积分（管理员）")
    @PostMapping("/updatePoints")
    public Result<Void> updatePoints(@RequestParam Long userId, @RequestParam Integer points) {
        sysUserService.updateUserPoints(userId, points);
        return Result.success();
    }
}
