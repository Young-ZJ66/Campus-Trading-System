package com.campus.controller;

import com.campus.common.Result;
import com.campus.pojo.PageResult;
import com.campus.pojo.SysNews;
import com.campus.service.SysNewsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "资讯接口")
@RestController
@RequestMapping("/api/news")
@CrossOrigin
public class SysNewsController {

    @Autowired
    private SysNewsService sysNewsService;

    @ApiOperation("资讯列表")
    @GetMapping("/list")
    public Result<PageResult<SysNews>> getList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(sysNewsService.getList(pageNum, pageSize));
    }

    @ApiOperation("资讯详情")
    @GetMapping("/detail/{id}")
    public Result<SysNews> getDetail(@PathVariable Long id) {
        return Result.success(sysNewsService.getDetail(id));
    }
}
