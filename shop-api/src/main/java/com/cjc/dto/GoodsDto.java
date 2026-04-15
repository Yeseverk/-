package com.cjc.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class GoodsDto {
    private Long id;               // 商品ID（编辑时使用）
    private String goodsName;      // 商品名称
    private String subTitle;       // 副标题
    private BigDecimal price;      // 价格
    private List<Long> categoryId; // 分类ID数组 [1,2,3]
    private Long brandId;          // 品牌ID
    private String image;          // 图片URL
    private String itemImages;     // 商品图片列表JSON（编辑回显用）
    private String introduction;   // 商品介绍（富文本）
    private String packageList;    // 包装清单
    private String afterService;   // 售后服务

    // ========== SKU 相关字段 ==========

    private String isEnableSpec;   // 是否启用规格：1-启用，0-不启用

    private String specificationItems;  // 规格选项JSON（存到 tb_goods_desc）

    private String customAttributeItems; // 扩展属性JSON（存到 tb_goods_desc）

    private List<TbItemDto> itemList;    // SKU列表（存到 tb_item）
}