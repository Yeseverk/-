package com.cjc.service;

import com.cjc.pojo.TbAreas;
import com.cjc.pojo.TbCities;
import com.cjc.pojo.TbProvinces;

import java.util.List;

public interface RegionService {

    /**
     * 获取所有省份
     */
    List<TbProvinces> getAllProvinces();

    /**
     * 根据省份ID获取城市列表
     */
    List<TbCities> getCitiesByProvinceId(String provinceId);

    /**
     * 根据城市ID获取区县列表
     */
    List<TbAreas> getAreasByCityId(String cityId);
}
