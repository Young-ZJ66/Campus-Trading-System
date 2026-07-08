package com.campus.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PointGoods {
    private Long itemId;
    private String name;
    private String description;
    private String image;
    private Integer pointsRequired;
    private Integer stock;
    private Integer status;
    private LocalDateTime createTime;
}