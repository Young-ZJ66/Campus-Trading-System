package com.campus.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.campus.common.Result;
import com.campus.pojo.GoodsComment;
import com.campus.service.GoodsCommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "商品评论接口")
@RestController
@RequestMapping("/api/comment")
@CrossOrigin
public class GoodsCommentController {

    @Autowired
    private GoodsCommentService goodsCommentService;

    @ApiOperation("发表评论")
    @PostMapping("/add")
    public Result<Void> addComment(@RequestBody GoodsComment comment) {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
        goodsCommentService.addComment(comment, userId);
        return Result.success();
    }

    @ApiOperation("评论列表")
    @GetMapping("/list/{goodsId}")
    public Result<List<GoodsComment>> getComments(@PathVariable Long goodsId) {
        return Result.success(goodsCommentService.getCommentsByGoodsId(goodsId));
    }
}
