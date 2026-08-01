package com.campus.pojo;

public enum OrderStatus {
       BARTER_PENDING(0, "待卖家同意"),

       WAIT_RECEIVE(1, "待收货/已同意"),
    COMPLETED(2, "已完成"),
    CANCELLED(3, "已取消"),

       BARTER_REJECTED(4, "已拒绝");

    private final int value;
    private final String desc;

    OrderStatus(int value, String desc) {
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
