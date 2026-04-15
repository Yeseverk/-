package com.cjc.service.impl;

import com.cjc.mapper.TbAreasMapper;
import com.cjc.mapper.TbCitiesMapper;
import com.cjc.mapper.TbProvincesMapper;
import com.cjc.pojo.TbAreas;
import com.cjc.pojo.TbCities;
import com.cjc.pojo.TbProvinces;
import com.cjc.service.RegionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class RegionServiceImpl implements RegionService {

    @Autowired
    private TbProvincesMapper provincesMapper;

    @Autowired
    private TbCitiesMapper citiesMapper;

    @Autowired
    private TbAreasMapper areasMapper;

    @Override
    public List<TbProvinces> getAllProvinces() {
        log.debug("查询所有省份");
        return provincesMapper.selectAll();
    }

    @Override
    public List<TbCities> getCitiesByProvinceId(String provinceId) {
        log.debug("查询省份[{}]下的城市列表", provinceId);
        return citiesMapper.selectByProvinceId(provinceId);
    }

    @Override
    public List<TbAreas> getAreasByCityId(String cityId) {
        log.debug("查询城市[{}]下的区县列表", cityId);
        return areasMapper.selectByCityId(cityId);
    }
}
