package com.campus.pojo;

import lombok.Data;

@Data
public class CreateOrderDTO {
    private Long goodsId;
    private Integer tradeType; // 0-普通购买, 1-以物换物
    private Long exchangeGoodsId; // 如果是以物换物，提供自己的商品ID
}