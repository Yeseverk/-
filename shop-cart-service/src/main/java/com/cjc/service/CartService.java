package com.cjc.service;

import com.cjc.vo.CartVo;

import java.util.List;

/**
 * 购物车服务接口（Redis版本）
 * 使用goodsId+itemId定位购物车商品，不再使用cartId
 */
public interface CartService {

    /**
     * 获取用户购物车列表（含商品详情）
     */
    List<CartVo> listWithGoods(Long userId);

    /**
     * 添加商品到购物车
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @param itemId SKU ID（可选）
     * @param num 数量
     */
    void add(Long userId, Long goodsId, Long itemId, Integer num);

    /**
     * 更新购物车商品数量
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @param itemId SKU ID
     * @param num 新数量
     */
    void updateNum(Long userId, Long goodsId, Long itemId, Integer num);

    /**
     * 删除购物车商品
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @param itemId SKU ID
     */
    void delete(Long userId, Long goodsId, Long itemId);

    /**
     * 批量删除购物车商品
     * @param userId 用户ID
     * @param items 商品列表 [{goodsId, itemId}]
     */
    void batchDelete(Long userId, List<String> items);

    /**
     * 获取购物车商品数量
     */
    Integer count(Long userId);

    /**
     * 清空购物车
     */
    void clear(Long userId);

    /**
     * 批量删除购物车商品（订单结算后使用）
     * @param userId 用户ID
     * @param goodsIds 商品ID列表
     * @param itemIds SKU ID列表
     */
    void batchDeleteByGoodsAndItem(Long userId, List<Long> goodsIds, List<Long> itemIds);
}