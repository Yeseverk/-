package com.cjc.mapper;

import com.cjc.pojo.TbAreas;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TbAreasMapper {

    @Select("SELECT * FROM tb_areas WHERE cityid = #{cityid} ORDER BY id")
    List<TbAreas> selectByCityId(@Param("cityid") String cityid);
}
