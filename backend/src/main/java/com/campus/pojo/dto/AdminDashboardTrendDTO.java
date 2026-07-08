package com.campus.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminDashboardTrendDTO {
    private String date;
    private long orderCount;
    private BigDecimal gmv;
    private long newUsers;
    private long newGoods;
}

