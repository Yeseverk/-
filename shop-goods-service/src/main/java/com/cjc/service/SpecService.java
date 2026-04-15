package com.cjc.service;

import com.cjc.dto.TbSpecificationDto;
import com.cjc.pojo.TbSpecification;
import com.cjc.query.QueryParams;
import com.cjc.util.PageList;
import com.cjc.vo.TbSpecificationVo;

import java.util.List;

public interface SpecService {

    /**
     * 分页操作
     * @return
     */
    PageList<TbSpecificationVo> queryPage(QueryParams<TbSpecification> params);

    /**
     * 根据id查询
     * @param specId
     * @return
     */
    TbSpecificationVo queryBySpecId(Long specId);

    /**
     * 根据id删除
     */
    void deleteById(Long id);

    /**
     * 批量删除
     */
    void batchDelete(List<Long> ids);

    /**
     * 添加
     */
    void insert(TbSpecificationDto tbSpecificationDto);

    /**
     * 修改
     */
    void update(TbSpecificationDto tbSpecificationDto);

    /**
     * 查询所有
     * @return
     */
    List<TbSpecificationVo> queryAll();
}
