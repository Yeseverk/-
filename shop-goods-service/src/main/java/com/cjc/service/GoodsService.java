package com.cjc.service;

import com.cjc.dto.GoodsDto;
import com.cjc.pojo.TbGoods;
import com.cjc.query.QueryParams;
import com.cjc.util.PageList;
import com.cjc.vo.TbGoodsVo;

import java.util.List;
import java.util.Map;

public interface GoodsService {
    // ========== 商家接口 ==========
    void save(GoodsDto goodsDto, String sellerId);

    PageList<TbGoodsVo> queryPage(QueryParams<TbGoods> queryParams, String sellerId);

    void delete(Long id, String sellerId);

    void batchDelete(List<Long> ids, String sellerId);

    GoodsDto getById(Long id, String sellerId);

    void update(GoodsDto goodsDto, String sellerId);

    void submitAudit(List<Long> ids, String sellerId);

    /**
     * 商家 - 上架商品（只有审核通过的商品才能上架）
     */
    void putOnSale(List<Long> ids, String sellerId);

    /**
     * 商家 - 下架商品
     */
    void pullOffSale(List<Long> ids, String sellerId);

    // ========== 运营商接口 ==========
    
    /**
     * 运营商 - 查询所有商品列表
     */
    PageList<TbGoodsVo> adminQueryPage(QueryParams<TbGoods> queryParams);

    /**
     * 运营商 - 商品详情（不检查上架状态）
     */
    Map<String, Object> getAdminDetailById(Long id);

    /**
     * 运营商 - 审核商品
     */
    void audit(Long id, String auditStatus);

    /**
     * 运营商 - 批量审核
     */
    void batchAudit(List<Long> ids, String auditStatus);

    /**
     * 运营商 - 批量删除
     */
    void adminBatchDelete(List<Long> ids);

    // ========== 前台公开接口 ==========

    /**
     * 前台 - 商品列表（只展示已上架且审核通过的商品）
     */
    PageList<TbGoodsVo> list(QueryParams<TbGoods> queryParams);

    /**
     * 前台 - 商品详情（只展示已上架且审核通过的商品）
     */
    Map<String, Object> getDetailById(Long id);

    // ========== 商家端规格模板接口 ==========

    /**
     * 商家端 - 根据分类ID获取类型模板
     */
    Map<String, Object> getTemplateByCategory(Long categoryId);

    /**
     * 商家端 - 根据规格ID列表获取规格详情
     */
    List<Map<String, Object>> getSpecsDetail(List<Long> specIds);
}
