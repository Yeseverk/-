package com.cjc.service;

import com.cjc.pojo.TbContent;
import com.cjc.pojo.TbContentCategory;
import com.cjc.query.QueryParams;
import com.cjc.util.PageList;
import com.cjc.vo.TbContentVo;
import com.cjc.vo.TbContentCategoryVo;

import java.util.List;

public interface ContentService {

    /**
     * 分页查询广告
     */
    PageList<TbContentVo> queryPage(QueryParams<TbContent> queryParams);

    /**
     * 添加广告
     */
    void insert(TbContent content);

    /**
     * 修改广告
     */
    void update(TbContent content);

    /**
     * 删除广告
     */
    void delete(Long id);

    /**
     * 批量删除广告
     */
    void batchDelete(List<Long> ids);

    /**
     * 查询所有广告
     */
    List<TbContentVo> queryAll();

    /**
     * 根据分类ID查询广告
     */
    List<TbContentVo> queryByCategoryId(Long categoryId);

    /**
     * 根据分类KEY查询广告（前台主站使用）
     */
    List<TbContentVo> queryByCategoryKey(String key);

    /**
     * 分页查询广告分类
     */
    PageList<TbContentCategoryVo> queryCategoryPage(QueryParams<TbContentCategory> queryParams);

    /**
     * 添加广告分类
     */
    void insertCategory(TbContentCategory category);

    /**
     * 修改广告分类
     */
    void updateCategory(TbContentCategory category);

    /**
     * 删除广告分类
     */
    void deleteCategory(Long id);

    /**
     * 查询所有广告分类
     */
    List<TbContentCategoryVo> queryAllCategory();
}