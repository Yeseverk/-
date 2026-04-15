package com.cjc.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 购物车VO - 包含商品详情
 */
@Data
public class CartVo {
    private Long id;              // 购物车ID
    private Long userId;          // 用户ID
    private Long goodsId;         // 商品ID
    private Long itemId;          // SKU ID
    private Integer num;          // 数量

    // 商品信息
    private String goodsName;     // 商品名称
    private String image;         // 商品图片
    private BigDecimal price;     // 商品价格
    private String spec;          // 规格信息
    private String sellerId;      // 商家ID
    private String sellerName;    // 商家名称
    private Integer stockCount;   // 库存

    // 计算字段
    private BigDecimal totalPrice; // 小计金额
    private Boolean checked;       // 是否选中（前端使用）
}
