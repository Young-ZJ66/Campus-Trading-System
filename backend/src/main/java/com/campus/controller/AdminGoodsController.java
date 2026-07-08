package com.campus.controller;

import com.campus.common.Result;
import com.campus.pojo.GoodsInfo;
import com.campus.pojo.GoodsQueryDTO;
import com.campus.pojo.PageResult;
import com.campus.service.GoodsInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "管理员-商品管理")
@RestController
@RequestMapping("/api/admin/goods")
@CrossOrigin
public class AdminGoodsController {

    @Autowired
    private GoodsInfoService goodsInfoService;

    @ApiOperation("商品列表（管理员）")
    @PostMapping("/list")
    public Result<PageResult<GoodsInfo>> list(@RequestBody GoodsQueryDTO query) {
        if (query.getStatus() == null) {
            query.setStatus(-1);
        }
        return Result.success(goodsInfoService.getList(query));
    }

    @ApiOperation("商品上下架（管理员）")
    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@RequestParam Long goodsId, @RequestParam Integer status) {
        goodsInfoService.updateStatusByAdmin(goodsId, status);
        return Result.success();
    }
}
