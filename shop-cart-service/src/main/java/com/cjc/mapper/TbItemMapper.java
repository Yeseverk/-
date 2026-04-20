package com.cjc.mapper;

import com.cjc.pojo.TbItem;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TbItemMapper {

    @Select("SELECT id, title, price, stock_count AS stockCount, image, spec, seller_id AS sellerId, goods_id AS goodsId, is_default AS isDefault FROM tb_item WHERE id = #{id}")
    TbItem selectItemById(@Param("id") Long id);

    /**
     * 批量查询SKU（解决N+1查询问题）
     */
    @Select("<script>SELECT id, title, price, stock_count AS stockCount, image, spec, seller_id AS sellerId, goods_id AS goodsId, is_default AS isDefault FROM tb_item WHERE id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<TbItem> selectItemByIds(@Param("ids") List<Long> ids);

    /**
     * 根据商品ID查询默认SKU
     */
    @Select("SELECT id, title, price, stock_count AS stockCount, image, spec, seller_id AS sellerId, goods_id AS goodsId, is_default AS isDefault FROM tb_item WHERE goods_id = #{goodsId} AND is_default = '1' LIMIT 1")
    TbItem selectDefaultItemByGoodsId(@Param("goodsId") Long goodsId);

    /**
     * 根据商品ID查询任意一个SKU（当没有默认SKU时使用）
     */
    @Select("SELECT id, title, price, stock_count AS stockCount, image, spec, seller_id AS sellerId, goods_id AS goodsId, is_default AS isDefault FROM tb_item WHERE goods_id = #{goodsId} LIMIT 1")
    TbItem selectFirstItemByGoodsId(@Param("goodsId") Long goodsId);
}
