package com.campus.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.campus.common.Result;
import com.campus.exception.GlobalException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Api(tags = "文件接口")
@RestController
@RequestMapping("/api/file")
@CrossOrigin
public class FileController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @ApiOperation("上传文件")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        StpUtil.checkLogin(); // 必须登录才能上传
        
        if (file.isEmpty()) {
            throw new GlobalException("上传文件不能为空");
        }

        // 获取原文件名及后缀
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 生成新文件名防止重复
        String newFilename = UUID.randomUUID().toString().replaceAll("-", "") + suffix;

        // 确保目录存在
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件
        try {
            file.transferTo(new File(dir, newFilename));
            // 返回可访问的 URL 路径
            String url = "/uploads/" + newFilename;
            return Result.success(url);
        } catch (IOException e) {
            e.printStackTrace();
            throw new GlobalException("文件上传失败");
        }
    }
}
