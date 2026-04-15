package com.cjc.dto;

import java.util.List;

/**
 * 创建订单请求DTO（Redis购物车版本）
 * 使用goodsId_itemId定位商品，不再使用cartId
 */
public class OrderCreateDto {

    // 商品列表（格式：goodsId_itemId）
    private List<String> items;

    // 收货地址ID
    private Long addressId;

    // 支付方式：1=微信，2=支付宝（本项目模拟支付）
    private String paymentType;

    // 买家留言
    private String buyerMessage;

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getBuyerMessage() {
        return buyerMessage;
    }

    public void setBuyerMessage(String buyerMessage) {
        this.buyerMessage = buyerMessage;
    }
}