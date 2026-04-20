package com.cjc.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 购物车VO - 包含商品详情（Redis版本）
 * id字段改为String类型，格式为goodsId_itemId
 */
@Data
public class CartVo {
    private String id;              // 购物车唯一标识（goodsId_itemId格式）
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

    // 商品状态字段（用于判断是否可购买）
    private String isMarketable;  // 上架状态 '1'=上架, '0'=下架
    private String auditStatus;   // 审核状态 0=未提交, 1=待审核, 2=审核通过, 3=审核驳回
    private String isDelete;      // 删除标记 '1'=已删除

    // 计算字段
    private BigDecimal totalPrice; // 小计金额
    private Boolean checked;       // 是否选中（前端使用）
    private Boolean valid;         // 商品是否有效（前端判断是否可勾选结算）
    private String statusMsg;      // 状态提示信息（如"商品已下架"、"商品已失效"）
}