package com.cjc.service.impl;

import com.cjc.mapper.TbTypeTemplateMapper;
import com.cjc.pojo.TbTypeTemplate;
import com.cjc.pojo.TbTypeTemplateExample;
import com.cjc.query.QueryParams;
import com.cjc.service.TypeTemplateService;
import com.cjc.util.PageList;
import com.cjc.vo.TbTypeTemplateVo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Autowired
    private TbTypeTemplateMapper tbTypeTemplateMapper;

    @Override
    public PageList<TbTypeTemplateVo> queryPage(QueryParams<TbTypeTemplate> params) {

        // 分页参数设置
        Page<TbTypeTemplate> page = PageHelper.startPage(params.getCurrentPage(), params.getPageSize());
        // 查询
        TbTypeTemplateExample tbTypeTemplateExample = new TbTypeTemplateExample();
        // 添加空值判断，防止 NPE
        if (params.getParams() != null && params.getParams().getName() != null && params.getParams().getName().trim().length() > 0) {
            tbTypeTemplateExample.createCriteria().andNameLike("%" + params.getParams().getName() + "%");
        }
        List<TbTypeTemplate> rows = tbTypeTemplateMapper.selectByExample(tbTypeTemplateExample);

        List<TbTypeTemplateVo> list = rows.stream().map(tbTypeTemplate -> {
            TbTypeTemplateVo tbTypeTemplateVo = new TbTypeTemplateVo();
            BeanUtils.copyProperties(tbTypeTemplate, tbTypeTemplateVo);
            return tbTypeTemplateVo;
        }).toList();

        PageList<TbTypeTemplateVo> pageList = new PageList<>(page.getTotal(), list);
        return pageList;
    }

    @Override
    public void deleteById(Long id) {
        tbTypeTemplateMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void insert(TbTypeTemplate tbTypeTemplate) {
        tbTypeTemplateMapper.insertSelective(tbTypeTemplate);
    }

    @Override
    public void update(TbTypeTemplate tbTypeTemplate) {
        tbTypeTemplateMapper.updateByPrimaryKeySelective(tbTypeTemplate);
    }
}
