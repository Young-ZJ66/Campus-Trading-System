package com.campus.pojo;

public enum TradeType {
    BUY(0, "普通购买"),
    BARTER(1, "以物换物");

    private final int value;
    private final String desc;

    TradeType(int value, String desc) {
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
