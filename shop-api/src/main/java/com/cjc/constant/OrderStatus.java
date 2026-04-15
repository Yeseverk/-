package com.cjc.constant;

public class OrderStatus {
    public static final String UNPAID = "1";
    public static final String PAID = "2";
    public static final String SHIPPED = "4";
    public static final String COMPLETED = "5";
    public static final String CLOSED = "6";

    public static String getText(String status) {
        switch (status) {
            case UNPAID: return "待付款";
            case PAID: return "待发货";
            case SHIPPED: return "已发货";
            case COMPLETED: return "已完成";
            case CLOSED: return "已关闭";
            default: return "未知";
        }
    }
}
