package com.campus.common;

/**
 * 分页查询参数校验工具，统一处理边界值
 */
public class PageQuery {

    /**
     * 校验并归一化页码
     */
    public static int normalizePageNum(Integer pageNum) {
        return Math.max(1, pageNum == null ? 1 : pageNum);
    }

    /**
     * 校验并归一化页大小，最大不超过 100
     */
    public static int normalizePageSize(Integer pageSize) {
        return Math.min(100, Math.max(1, pageSize == null ? 10 : pageSize));
    }

    /**
     * 计算 offset
     */
    public static int offset(int pageNum, int pageSize) {
        return (pageNum - 1) * pageSize;
    }
}