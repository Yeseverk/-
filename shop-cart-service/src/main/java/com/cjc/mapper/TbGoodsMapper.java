package com.cjc.mapper;

import com.cjc.pojo.TbGoods;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface TbGoodsMapper {

    @Select("SELECT id, seller_id AS sellerId, goods_name AS goodsName, default_item_id AS defaultItemId, small_pic AS smallPic, price, is_marketable AS isMarketable, audit_status AS auditStatus, is_delete AS isDelete FROM tb_goods WHERE id = #{id}")
    TbGoods selectGoodsById(@Param("id") Long id);
}
