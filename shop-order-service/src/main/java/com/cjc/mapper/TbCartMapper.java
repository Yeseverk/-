package com.cjc.mapper;

import com.cjc.pojo.TbCart;
import com.cjc.pojo.TbCartExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 购物车Mapper（订单服务使用）
 */
public interface TbCartMapper {

    int countByExample(TbCartExample example);

    int deleteByExample(TbCartExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TbCart record);

    int insertSelective(TbCart record);

    List<TbCart> selectByExample(TbCartExample example);

    TbCart selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TbCart record, @Param("example") TbCartExample example);

    int updateByExample(@Param("record") TbCart record, @Param("example") TbCartExample example);

    int updateByPrimaryKeySelective(TbCart record);

    int updateByPrimaryKey(TbCart record);

    /**
     * 批量删除购物车
     */
    int batchDelete(@Param("ids") List<Long> ids, @Param("userId") String userId);
}