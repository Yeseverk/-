package com.cjc.service;

import com.cjc.pojo.TbTypeTemplate;
import com.cjc.query.QueryParams;
import com.cjc.util.PageList;
import com.cjc.vo.TbTypeTemplateVo;

public interface TypeTemplateService {
    /**
     * 分页操作
     * @return
     */
    PageList<TbTypeTemplateVo> queryPage(QueryParams<TbTypeTemplate> params);

    /**
     * 删除操作
     */
    void deleteById(Long id);

    /**
     * 添加操作
     */
    void insert(TbTypeTemplate tbTypeTemplate);

    /**
     * 修改操作
     */
    void update(TbTypeTemplate tbTypeTemplate);
}
