package com.campus.pojo;

import lombok.Data;

@Data
public class GoodsQueryDTO {
    private String keyword;
    private Long categoryId;
    private Integer isExchange; // 1-仅看支持换物
    private Integer sortType; // 0-最新, 1-价格最低, 2-热度最高
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private Long userId; // 发布者ID过滤
    private Integer status; // 状态过滤 (0: 在售, 1: 已售出, 2: 已下架)

    /**
     * 获取校正后的页码（防止负数或0）
     */
    public int getSafePageNum() {
        if (pageNum == null || pageNum < 1) {
            return 1;
        }
        return pageNum;
    }

    /**
     * 获取校正后的每页条数（限制 1~100）
     */
    public int getSafePageSize() {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        if (pageSize > 100) {
            return 100;
        }
        return pageSize;
    }
}
