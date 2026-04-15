package com.cjc.constant;

public class PayStatus {
    public static final String UNPAID = "0";
    public static final String PAID = "1";

    public static String getText(String status) {
        switch (status) {
            case UNPAID: return "未支付";
            case PAID: return "已支付";
            default: return "未知";
        }
    }
}
