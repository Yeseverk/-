package com.cjc.mapper;

import com.cjc.pojo.TbGoods;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TbGoodsMapper {

    @Select("SELECT id, seller_id AS sellerId, goods_name AS goodsName, default_item_id AS defaultItemId, small_pic AS smallPic, price, is_marketable AS isMarketable, audit_status AS auditStatus, is_delete AS isDelete FROM tb_goods WHERE id = #{id}")
    TbGoods selectGoodsById(@Param("id") Long id);

    /**
     * 批量查询商品（解决N+1查询问题）
     */
    @Select("<script>SELECT id, seller_id AS sellerId, goods_name AS goodsName, default_item_id AS defaultItemId, small_pic AS smallPic, price, is_marketable AS isMarketable, audit_status AS auditStatus, is_delete AS isDelete FROM tb_goods WHERE id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<TbGoods> selectGoodsByIds(@Param("ids") List<Long> ids);
}
