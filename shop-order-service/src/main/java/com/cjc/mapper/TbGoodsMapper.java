package com.cjc.mapper;

import com.cjc.pojo.TbGoods;

/**
 * 商品 Mapper（订单服务使用）
 */
public interface TbGoodsMapper {

    /**
     * 根据ID查询商品基本信息
     */
    TbGoods selectByPrimaryKey(Long goodsId);

    /**
     * 检查商品是否可购买（上架+审核通过）
     */
    TbGoods selectForOrder(Long goodsId);
}