package com.cjc.controller;

import com.cjc.service.ItemCatService;
import com.cjc.util.Result;
import com.cjc.vo.TbItemCatVo;
import com.cjc.vo.TbTypeTemplateVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "商品分类管理",description = "商品分类管理接口")
@RestController
@RequestMapping("/item")
public class ItemCatController {
    @Autowired
    private ItemCatService itemCatService;

    @GetMapping("/queryAll")
    public Result<List<Map<String, Object>>> queryAll() {
        List<Map<String, Object>> maps = itemCatService.queryAll();
        return Result.success(maps);

    }
    
    /**
     * 新增分类
     */
    @PostMapping("/add")
    public Result add(@RequestBody TbItemCatVo itemCatVo){
        itemCatService.add(itemCatVo);
        return Result.success();
    }
    
    /**
     * 修改分类
     */
    @PostMapping("/update")
    public Result update(@RequestBody TbItemCatVo itemCatVo){
        itemCatService.update(itemCatVo);
        return Result.success();
    }
    
    /**
     * 删除分类
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id){
        itemCatService.delete(id);
        return Result.success();
    }
    
    /**
     * 根据id查询关联模板
     */
    @GetMapping("/queryTemplateById/{id}")
    public Result<TbTypeTemplateVo> queryTemplateById(@PathVariable("id") Long id){
        TbTypeTemplateVo typeTemplateVo = itemCatService.queryTemplateById(id);
        return Result.success(typeTemplateVo);
    }

}
