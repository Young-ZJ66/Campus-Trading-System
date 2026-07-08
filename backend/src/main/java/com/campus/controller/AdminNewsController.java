package com.campus.controller;

import com.campus.common.Result;
import com.campus.pojo.PageResult;
import com.campus.pojo.SysNews;
import com.campus.service.SysNewsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "管理员-资讯管理")
@RestController
@RequestMapping("/api/admin/news")
@CrossOrigin
public class AdminNewsController {

    @Autowired
    private SysNewsService sysNewsService;

    @ApiOperation("资讯列表（管理员）")
    @GetMapping("/list")
    public Result<PageResult<SysNews>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isTop) {
        return Result.success(sysNewsService.getAdminList(pageNum, pageSize, keyword, status, isTop));
    }

    @ApiOperation("新增资讯")
    @PostMapping("/create")
    public Result<SysNews> create(@RequestBody SysNews news) {
        return Result.success(sysNewsService.create(news));
    }

    @ApiOperation("编辑资讯")
    @PostMapping("/update")
    public Result<Void> update(@RequestBody SysNews news) {
        sysNewsService.update(news);
        return Result.success();
    }

    @ApiOperation("删除资讯")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long newsId) {
        sysNewsService.delete(newsId);
        return Result.success();
    }

    @ApiOperation("资讯上下架")
    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@RequestParam Long newsId, @RequestParam Integer status) {
        sysNewsService.updateStatus(newsId, status);
        return Result.success();
    }

    @ApiOperation("资讯置顶/取消置顶")
    @PostMapping("/updateTop")
    public Result<Void> updateTop(@RequestParam Long newsId, @RequestParam Integer isTop) {
        sysNewsService.updateTop(newsId, isTop);
        return Result.success();
    }
}
