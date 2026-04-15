package com.cjc.service;

import com.cjc.vo.TbItemCatVo;
import com.cjc.vo.TbTypeTemplateVo;

import java.util.List;
import java.util.Map;

public interface ItemCatService {

    List<Map<String, Object>> queryAll();

    /**
     * 新增分类（自动计算 parentPath）
     */
    void add(TbItemCatVo itemCatVo);

    /**
     * 修改分类（自动计算 parentPath）
     */
    void update(TbItemCatVo itemCatVo);

    /**
     * 删除分类
     */
    void delete(Long id);

    TbTypeTemplateVo queryTemplateById(Long id);
}
