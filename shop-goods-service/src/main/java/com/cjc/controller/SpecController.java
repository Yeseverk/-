package com.cjc.controller;

import com.cjc.dto.TbSpecificationDto;
import com.cjc.pojo.TbSpecification;
import com.cjc.query.QueryParams;
import com.cjc.service.SpecService;
import com.cjc.util.PageList;
import com.cjc.util.Result;
import com.cjc.vo.TbSpecificationVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/spec")
public class SpecController {

    @Autowired
    private SpecService specService;

    /**
     * 分页操作
     * @todo:   没有处理异常。
     */
    @PostMapping("/queryPage")
    public Result<PageList<TbSpecificationVo>> queryPage(@RequestBody QueryParams<TbSpecification> params) {

        PageList<TbSpecificationVo> pageList = specService.queryPage(params);

        return Result.success(pageList);
    }

    /**
     * 根据规格id，查询规格和具体的规格集合
     */
    @GetMapping("/queryBySpecId/{specId}")
    public Result<TbSpecificationVo> queryBySpecId(@PathVariable("specId") Long specId) {

        TbSpecificationVo tbSpecificationVo = specService.queryBySpecId(specId);

        return Result.success(tbSpecificationVo);
    }

    /**
     * 根据id删除
     */
    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable("id") Long id){
        specService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @PatchMapping("/batchDelete")
    public Result batchDelete(@RequestBody List<Long> ids){
        specService.batchDelete(ids);
        return Result.success();
    }

    /**
     * 保存操作
     */
    @PostMapping("/save")
    public Result save(@RequestBody TbSpecificationDto tbSpecificationDto){
        if(tbSpecificationDto.getId()==null){
            // 添加操作
            specService.insert(tbSpecificationDto);
        }else{
            // 修改操作
            specService.update(tbSpecificationDto);
        }


        return Result.success();
    }

    /**
     * 查询所有
     */
    @GetMapping("/queryAll")
    public Result<List<TbSpecificationVo>> queryAll(){
        List<TbSpecificationVo> tbSpecificationVos = specService.queryAll();
        return Result.success(tbSpecificationVos);
    }
}
