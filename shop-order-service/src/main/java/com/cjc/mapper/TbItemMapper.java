package com.cjc.mapper;

import com.cjc.pojo.TbItem;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * SKU Mapper（订单服务使用）
 */
public interface TbItemMapper {

    /**
     * 根据ID查询SKU
     */
    TbItem selectByPrimaryKey(Long id);

    /**
     * 根据商品ID查询默认SKU
     */
    TbItem selectDefaultItemByGoodsId(Long goodsId);

    /**
     * 根据商品ID查询任意一个SKU（兜底）
     */
    TbItem selectFirstItemByGoodsId(Long goodsId);

    /**
     * 乐观锁扣减库存
     * @return 更新行数，0表示库存不足或SKU不存在
     */
    int deductStock(@Param("itemId") Long itemId, @Param("num") Integer num);

    /**
     * 释放库存（取消订单）
     */
    int releaseStock(@Param("itemId") Long itemId, @Param("num") Integer num);

    /**
     * 更新SKU销量
     */
    int updateSales(@Param("itemId") Long itemId, @Param("num") Integer num);
}