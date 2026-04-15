package com.cjc.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 商品列表VO - 用于商品管理页面展示
 */
@Data
public class TbGoodsVo {
    // 商品基本信息
    private Long id;              // 商品ID
    private String sellerId;      // 商家ID
    private String goodsName;     // 商品名称
    private BigDecimal price;     // 商品价格
    private String smallPic;      // 缩略图
    private String auditStatus;   // 审核状态：0未提交 1待审核 2审核通过 3审核驳回
    private String isMarketable;  // 上架状态：1上架 0下架
    
    // 分类ID
    private Long category1Id;     // 一级分类ID
    private Long category2Id;     // 二级分类ID
    private Long category3Id;     // 三级分类ID
    
    // 分类名称（关联查询）
    private String category1Name; // 一级分类名称
    private String category2Name; // 二级分类名称
    private String category3Name; // 三级分类名称
}