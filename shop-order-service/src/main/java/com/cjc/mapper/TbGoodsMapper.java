package com.cjc.mapper;

import com.cjc.pojo.TbGoods;

import java.util.List;

/**
 * 商品 Mapper（订单服务使用）
 */
public interface TbGoodsMapper {

    /**
     * 根据ID查询商品基本信息
     */
    TbGoods selectByPrimaryKey(Long goodsId);

    /**
     * 批量查询商品（解决N+1查询问题）
     */
    List<TbGoods> selectGoodsByIds(List<Long> ids);

    /**
     * 检查商品是否可购买（上架+审核通过+未删除）
     */
    TbGoods selectForOrder(Long goodsId);
}