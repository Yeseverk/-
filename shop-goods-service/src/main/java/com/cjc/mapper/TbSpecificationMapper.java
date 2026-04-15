package com.cjc.mapper;

import com.cjc.pojo.TbSpecification;
import com.cjc.pojo.TbSpecificationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TbSpecificationMapper {
    int countByExample(TbSpecificationExample example);

    int deleteByExample(TbSpecificationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TbSpecification record);

    int insertSelective(TbSpecification record);

    List<TbSpecification> selectByExample(TbSpecificationExample example);

    TbSpecification selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TbSpecification record, @Param("example") TbSpecificationExample example);

    int updateByExample(@Param("record") TbSpecification record, @Param("example") TbSpecificationExample example);

    int updateByPrimaryKeySelective(TbSpecification record);

    int updateByPrimaryKey(TbSpecification record);

    /**
     * 添加规格，返回自增的主键
     * @param tbSpecification
     */
    void add(TbSpecification tbSpecification);
}