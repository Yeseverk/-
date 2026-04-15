package com.cjc.controller;

import com.cjc.pojo.TbAreas;
import com.cjc.pojo.TbCities;
import com.cjc.pojo.TbProvinces;
import com.cjc.service.RegionService;
import com.cjc.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "省市区管理", description = "省市区查询接口")
@RestController
@RequestMapping("/region")
public class RegionController {

    @Autowired
    private RegionService regionService;

    @Operation(summary = "获取所有省份")
    @GetMapping("/provinces")
    public Result getAllProvinces() {
        List<TbProvinces> provinces = regionService.getAllProvinces();
        return Result.success(provinces);
    }

    @Operation(summary = "根据省份ID获取城市列表")
    @GetMapping("/cities/{provinceId}")
    public Result getCitiesByProvinceId(@PathVariable String provinceId) {
        List<TbCities> cities = regionService.getCitiesByProvinceId(provinceId);
        return Result.success(cities);
    }

    @Operation(summary = "根据城市ID获取区县列表")
    @GetMapping("/areas/{cityId}")
    public Result getAreasByCityId(@PathVariable String cityId) {
        List<TbAreas> areas = regionService.getAreasByCityId(cityId);
        return Result.success(areas);
    }
}
