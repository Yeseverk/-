package com.cjc.dto;

import lombok.Data;
import java.util.Date;

/**
 * Redis购物车商品项（内部使用）
 * 存储在Redis Hash中的数据结构
 */
@Data
public class CartItem {
    private Long goodsId;      // 商品ID
    private Long itemId;       // SKU ID
    private Integer num;       // 数量
    private Date createTime;   // 创建时间
    private Date updateTime;   // 更新时间
}