package com.cjc.mapper;

import com.cjc.pojo.TbProvinces;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TbProvincesMapper {

    @Select("SELECT * FROM tb_provinces ORDER BY id")
    List<TbProvinces> selectAll();
}
