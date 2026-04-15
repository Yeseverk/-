package com.cjc.mapper;

import com.cjc.pojo.TbCities;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TbCitiesMapper {

    @Select("SELECT * FROM tb_cities WHERE provinceid = #{provinceid} ORDER BY id")
    List<TbCities> selectByProvinceId(@Param("provinceid") String provinceid);
}
