package com.cjc.vo;

import com.cjc.pojo.TbOrder;
import com.cjc.pojo.TbOrderItem;

import java.util.List;

/**
 * 订单详情VO
 */
public class OrderVo extends TbOrder {

    // 订单商品列表
    private List<TbOrderItem> orderItems;

    // 订单状态描述
    private String statusDesc;

    // 商家名称（可选）
    private String sellerName;

    public List<TbOrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<TbOrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public String getStatusDesc() {
        if (getStatus() == null) return "";
        switch (getStatus()) {
            case "1": return "未付款";
            case "2": return "已付款";
            case "3": return "未发货";
            case "4": return "已发货";
            case "5": return "交易成功";
            case "6": return "交易关闭";
            default: return "";
        }
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }
}