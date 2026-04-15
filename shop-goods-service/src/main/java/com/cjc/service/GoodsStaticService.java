package com.cjc.service;

import java.util.List;
import java.util.Map;

/**
 * 商品详情页静态化服务
 */
public interface GoodsStaticService {

    /**
     * 生成商品详情静态页
     * @param goodsId 商品ID
     * @return 静态页URL
     */
    String generateStaticPage(Long goodsId);

    /**
     * 重新生成静态页（删除旧版本）
     * @param goodsId 商品ID
     * @param oldStaticUrl 旧静态页URL
     * @return 新静态页URL
     */
    String regenerateStaticPage(Long goodsId, String oldStaticUrl);

    /**
     * 删除商品静态页
     * @param goodsId 商品ID
     * @param staticUrl 静态页URL
     */
    void deleteStaticPage(Long goodsId, String staticUrl);

    /**
     * 批量生成静态页
     * @param goodsIds 商品ID列表
     */
    void batchGenerateStaticPage(List<Long> goodsIds);

    /**
     * 批量生成静态页（带结果返回）
     * @param goodsIds 商品ID列表
     * @return 生成结果 {total, success, fail, successList, failList}
     */
    Map<String, Object> batchGenerateStaticPageWithResult(List<Long> goodsIds);
}
