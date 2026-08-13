package com.campus.common;

import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 上传目录定位器：无论从项目根目录还是 backend 子目录启动，
 * 都解析到 backend/uploads/，避免因启动目录不同导致图片路径错位。
 * 基于运行时 user.dir，项目迁移后自动跟随，无需配置文件路径。
 */
@Component
public class UploadPathResolver {

    private static final String UPLOADS_DIR = "uploads";

    /**
     * 返回上传目录绝对路径（以分隔符结尾）
     */
    public String resolve() {
        String userDir = System.getProperty("user.dir");

        // 1. 从 backend 目录启动：user.dir/uploads
        File direct = new File(userDir, UPLOADS_DIR);
        if (direct.isDirectory()) {
            return normalize(direct.getAbsolutePath());
        }

        // 2. 从项目根目录启动：user.dir/backend/uploads
        File underBackend = new File(userDir, "backend" + File.separator + UPLOADS_DIR);
        if (underBackend.isDirectory()) {
            return normalize(underBackend.getAbsolutePath());
        }

        // 3. 兜底：基于当前目录，由上传接口负责创建
        return normalize(new File(userDir, UPLOADS_DIR).getAbsolutePath());
    }

    private String normalize(String path) {
        return path.endsWith("/") || path.endsWith("\\") ? path : path + "/";
    }
}