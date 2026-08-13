package com.campus.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.campus.common.Result;
import com.campus.exception.GlobalException;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Tag(name = "文件接口")
@RestController
@RequestMapping("/api/file")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    /** 允许上传的文件后缀白名单 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp"
    );

    /** 允许上传的 Content-Type 白名单 */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    );

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        StpUtil.checkLogin();

        if (file.isEmpty()) {
            throw new GlobalException("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(suffix)) {
            throw new GlobalException("不支持的文件类型，仅允许上传图片(jpg/jpeg/png/gif/webp/bmp)");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new GlobalException("文件内容类型不合法");
        }

        String newFilename = UUID.randomUUID().toString().replaceAll("-", "") + suffix;

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            byte[] bytes = file.getBytes();
            if (!isImage(bytes)) {
                throw new GlobalException("文件内容不是有效图片");
            }
            Files.write(Paths.get(dir.getAbsolutePath(), newFilename), bytes);
            String url = "/uploads/" + newFilename;
            return Result.success(url);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new GlobalException("文件上传失败");
        }
    }

    /**
     * 通过文件头魔数校验真实图片类型，防止伪造扩展名/Content-Type
     */
    private boolean isImage(byte[] bytes) {
        if (bytes == null || bytes.length < 4) {
            return false;
        }
        // PNG: 89 50 4E 47
        if ((bytes[0] & 0xFF) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G') {
            return true;
        }
        // JPEG: FF D8 FF
        if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return true;
        }
        // GIF: 47 49 46 38
        if (bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == '8') {
            return true;
        }
        // BMP: 42 4D
        if (bytes[0] == 'B' && bytes[1] == 'M') {
            return true;
        }
        // WebP: RIFF .... WEBP (offset 8)
        if (bytes.length >= 12 && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return true;
        }
        return false;
    }
}
