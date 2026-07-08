package com.campus.pojo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoodsInfo {
    private Long goodsId;
    private Long userId;
    private Long categoryId;
    private String title;
    private String description;
    private String images; // 数据库中是 JSON，这里作为 String 处理
    private BigDecimal originalPrice;
    private BigDecimal price;
    private Integer isExchange;
    private String exchangeDesc;
    private Integer status;
    private Integer viewCount;
    private LocalDateTime createTime;
    
    // 扩展字段用于前端展示
    private String publisherName;
    private String publisherAvatar;
    private String categoryName;
}