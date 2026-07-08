package com.campus.pojo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoodsOrder {
    private Long orderId;
    private String orderNo;
    private Long buyerId;
    private Long sellerId;
    private Long goodsId;
    private BigDecimal amount;
    private Integer tradeType; // 0-普通购买, 1-以物换物
    private Long exchangeGoodsId; // 交换商品ID
    private Integer status; // 普通购买: 1-待收货, 2-已完成, 3-已取消; 以物换物: 0-待卖家同意, 1-已同意, 2-已完成, 3-已取消, 4-已拒绝
    private LocalDateTime createTime;
    
    // 扩展字段
    private String goodsTitle;
    private String goodsImage;
    private String exchangeGoodsTitle;
    private String exchangeGoodsImage;
    private Integer goodsStatus;
    private Integer exchangeGoodsStatus;
    private String buyerName;
    private String sellerName;
}
