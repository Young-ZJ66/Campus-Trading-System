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
}