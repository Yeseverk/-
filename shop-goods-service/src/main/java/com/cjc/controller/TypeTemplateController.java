package com.cjc.controller;

import com.cjc.pojo.TbTypeTemplate;
import com.cjc.query.QueryParams;
import com.cjc.service.TypeTemplateService;
import com.cjc.util.PageList;
import com.cjc.util.Result;
import com.cjc.vo.TbTypeTemplateVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/typeTemplate")
public class TypeTemplateController {

    @Autowired
    private TypeTemplateService templateService;

    /**
     * 分页操作
     */
    @PostMapping("/queryPage")
    public Result<PageList<TbTypeTemplateVo>> queryPage(@RequestBody QueryParams<TbTypeTemplate> params) {
        PageList<TbTypeTemplateVo> pageList = templateService.queryPage(params);

        return Result.success(pageList);
    }

    /**
     * 删除操作
     */
    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable("id") Long id) {
        templateService.deleteById(id);
        return Result.success();
    }

    /**
     * 保存操作
     */
    @PostMapping("/save")
    public Result save(@RequestBody TbTypeTemplate tbTypeTemplate){
        if(tbTypeTemplate.getId()==null){
            // 添加操作
            templateService.insert(tbTypeTemplate);
        }else{
            // 修改操作
            templateService.update(tbTypeTemplate);
        }

        return Result.success();
    }
}
