package com.cjc.controller;

import com.cjc.aop.AnnoLog;
import com.cjc.pojo.TbBrand;
import com.cjc.query.QueryParams;
import com.cjc.service.BrandService;
import com.cjc.util.PageList;
import com.cjc.util.Result;
import com.cjc.vo.TbBrandVo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "品牌管理",description = "品牌管理接口")
@RestController
@RequestMapping("/brand")
public class BrandController {

    @Autowired
    private BrandService brandService;


    /**
     * 保存操作： 添加和修改
     */
    @Operation(summary = "保存操作",description = "添加修改操作")
    @PostMapping("/save")
    public Result save(@RequestBody TbBrand tbBrand){
        if(tbBrand.getId()==null){
            // 添加
            brandService.insert(tbBrand);
        }else{
            // 修改
            brandService.update(tbBrand);
        }

        return Result.success();
    }

    /**
     * 分页查询
     */
    @AnnoLog
    @PostMapping("/queryPage")
    public Result<PageList<TbBrandVo>> queryPage(@RequestBody QueryParams<TbBrand> queryParams) {
        PageList<TbBrandVo> pageList = brandService.queryPage(queryParams);
        return Result.success(pageList);
    }

    /**
     * 添加
     */
    @PostMapping("/insert")
    public Result insert(@RequestBody TbBrand tbBrand){
        brandService.insert(tbBrand);
        return Result.success();
    }

    /**
     * 修改操作
     */
    @PostMapping("/update")
    public Result update(@RequestBody TbBrand tbBrand){
        brandService.update(tbBrand);
        return Result.success();
    }

    /**
     * 删除
     */
    @Operation(summary = "删除操作",description = "删除操作")
    @DeleteMapping("/delete/{id}")
    public Result delete(@Parameter(
            description = "用户ID", // 描述信息
            required = true, // 是否必填
            example = "1001" // 示例值
    )@PathVariable("id") Long id){
        brandService.delete(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @PatchMapping("/batchDelete")
    public Result batchDelete(@RequestBody List<Long> ids){
        brandService.batchDelete(ids);
        return Result.success();
    }

    /**
     * 查询所有
     */
    @Operation(summary = "查询所有",description = "查询所有品牌")
    @GetMapping("/queryAll")
    public Result<List<TbBrandVo>> queryAll(){
        List<TbBrandVo> list = brandService.queryAll();
        return Result.success(list);
    }
}
