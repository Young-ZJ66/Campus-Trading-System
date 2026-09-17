package com.campus.pojo;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoodsInfo {
    private Long goodsId;
    private Long userId;
    @NotNull(message = "分类不能为空")
    private Long categoryId;
    @NotBlank(message = "商品标题不能为空")
    @Size(max = 100, message = "商品标题最长100个字符")
    private String title;
    @Size(max = 2000, message = "商品描述最长2000个字符")
    private String description;
    @Size(max = 5000, message = "图片数据过长")
    private String images; // 数据库中是 JSON，这里作为 String 处理
    @DecimalMin(value = "0.01", message = "原价必须大于0")
    private BigDecimal originalPrice;
    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.01", message = "售价必须大于0")
    private BigDecimal price;
    private Integer isExchange;
    @Size(max = 500, message = "交换描述最长500个字符")
    private String exchangeDesc;
    private Integer status;
    private Integer viewCount;
    private LocalDateTime createTime;

    private String publisherName;
    private String publisherAvatar;
    private String categoryName;
}