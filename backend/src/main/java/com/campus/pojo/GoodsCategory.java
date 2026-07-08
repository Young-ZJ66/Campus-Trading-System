package com.campus.pojo;

import lombok.Data;

@Data
public class GoodsCategory {
    private Long categoryId;
    private String name;
    private Long parentId;
    private Integer sort;
    private Integer status;
}