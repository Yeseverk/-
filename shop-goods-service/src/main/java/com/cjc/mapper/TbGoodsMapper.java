package com.cjc.mapper;

import com.cjc.pojo.TbGoods;
import com.cjc.pojo.TbGoodsExample;
import com.cjc.vo.TbGoodsVo;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TbGoodsMapper {
    int countByExample(TbGoodsExample example);

    int deleteByExample(TbGoodsExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TbGoods record);

    int insertSelective(TbGoods record);

    List<TbGoods> selectByExample(TbGoodsExample example);

    TbGoods selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TbGoods record, @Param("example") TbGoodsExample example);

    int updateByExample(@Param("record") TbGoods record, @Param("example") TbGoodsExample example);

    int updateByPrimaryKeySelective(TbGoods record);

    int updateByPrimaryKey(TbGoods record);

    /**
     * 商品列表关联查询 - 包含分类名称
     */
    List<TbGoodsVo> selectGoodsVoList(@Param("sellerId") String sellerId, 
                                       @Param("goodsName") String goodsName, 
                                       @Param("auditStatus") String auditStatus);

    /**
     * 批量逻辑删除商品（带商家权限校验）
     */
    void logicalBatchDelete(@Param("ids") List<Long> ids, @Param("sellerId") String sellerId);

    /**
     * 批量提交审核（只有未审核和已驳回的才能提交）
     */
    void submitAudit(@Param("ids") List<Long> ids, @Param("sellerId") String sellerId);

    /**
     * 商家 - 上架商品（只有审核通过的商品才能上架）
     */
    void putOnSale(@Param("ids") List<Long> ids, @Param("sellerId") String sellerId);

    /**
     * 商家 - 下架商品
     */
    void pullOffSale(@Param("ids") List<Long> ids, @Param("sellerId") String sellerId);

    // ========== 运营商接口 ==========

    /**
     * 运营商 - 批量审核（只更新待审核状态的商品）
     */
    void adminBatchAudit(@Param("ids") List<Long> ids, @Param("auditStatus") String auditStatus);

    /**
     * 运营商 - 批量逻辑删除
     */
    void adminBatchDelete(@Param("ids") List<Long> ids);

    /**
     * 批量清空静态页URL
     */
    void batchClearStaticUrl(@Param("ids") List<Long> ids);

    // ========== 前台公开接口 ==========

    /**
     * 前台 - 查询已上架且审核通过的商品列表
     * 条件：isMarketable='1' AND auditStatus='2' AND isDelete='0'
     */
    List<TbGoodsVo> selectPublishedGoods(@Param("goodsName") String goodsName,
                                          @Param("category1Id") Long category1Id,
                                          @Param("minPrice") Double minPrice,
                                          @Param("maxPrice") Double maxPrice);
}