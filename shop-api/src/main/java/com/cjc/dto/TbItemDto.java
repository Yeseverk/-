package com.cjc.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * SKU 数据传输对象
 * 用于前端传递每个 SKU 的价格、库存、规格等信息
 */
@Data
public class TbItemDto {
    
    // 规格组合（前端传递的 JSON 对象字符串）
    // 格式：{"网络":"移动4G","内存":"128G"}
    private String spec;
    
    // SKU 价格
    private BigDecimal price;
    
    // SKU 库存
    private Integer num;
    
    // 是否启用
    private Boolean enabled;
    
    // 是否默认 SKU
    // 注意：使用 defaultFlag 避开 Boolean isXxx 的 JavaBeans 命名问题
    private Boolean defaultFlag;
}