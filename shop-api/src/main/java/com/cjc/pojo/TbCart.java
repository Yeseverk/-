package com.cjc.pojo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.Date;

/**
 * 购物车实体类
 */
public class TbCart {
    // 使用ToStringSerializer避免JavaScript精度丢失
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;           // 主键ID
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;       // 用户ID
    @JsonSerialize(using = ToStringSerializer.class)
    private Long goodsId;      // 商品ID
    @JsonSerialize(using = ToStringSerializer.class)
    private Long itemId;       // SKU ID
    private Integer num;       // 商品数量
    private Date createTime;   // 创建时间
    private Date updateTime;   // 更新时间

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}