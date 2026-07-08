package com.campus.pojo;

public enum GoodsStatus {
    ON_SALE(0, "在售"),
    SOLD(1, "已售出"),
    OFF_SHELF(2, "已下架"),
    BARTER_PENDING(3, "置换锁定中");

    private final int value;
    private final String desc;

    GoodsStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}
