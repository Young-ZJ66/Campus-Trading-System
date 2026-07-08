package com.campus.pojo.dto;

import lombok.Data;

@Data
public class AdminDashboardStatsDTO {
    private long userTotal;
    private long userToday;
    private long goodsTotal;
    private long goodsOnSale;
    private long goodsToday;
    private long orderTotal;
    private long orderToday;
    private long orderCompletedTotal;
    private long orderCompletedToday;
    private java.math.BigDecimal gmvTotal;
    private java.math.BigDecimal gmvToday;
    private long pointGoodsTotal;
    private long pointOrderTotal;
    private long pointOrderToday;
}
