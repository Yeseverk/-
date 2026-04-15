package com.cjc.service.impl;

import com.cjc.dto.TbSpecificationDto;
import com.cjc.mapper.TbSpecificationMapper;
import com.cjc.mapper.TbSpecificationOptionMapper;
import com.cjc.pojo.TbSpecification;
import com.cjc.pojo.TbSpecificationExample;
import com.cjc.pojo.TbSpecificationOption;
import com.cjc.pojo.TbSpecificationOptionExample;
import com.cjc.query.QueryParams;
import com.cjc.service.SpecService;
import com.cjc.util.PageList;
import com.cjc.vo.TbSpecificationOptionVo;
import com.cjc.vo.TbSpecificationVo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SpecServiceImpl implements SpecService {

    @Autowired
    private TbSpecificationMapper tbSpecificationMapper;

    @Autowired
    private TbSpecificationOptionMapper tbSpecificationOptionMapper;

    @Override
    public PageList<TbSpecificationVo> queryPage(QueryParams<TbSpecification> params) {

        // 分页参数设置
        Page<TbSpecification> page = PageHelper.startPage(params.getCurrentPage(), params.getPageSize());

        // 查询
        TbSpecificationExample tbSpecificationExample = new TbSpecificationExample();
        if(params.getParams()!=null && params.getParams().getSpecName()!=null && params.getParams().getSpecName().trim().length()>0){
            tbSpecificationExample.createCriteria().andSpecNameLike("%" + params.getParams().getSpecName() + "%");
        }

        List<TbSpecification> tbSpecifications = tbSpecificationMapper.selectByExample(tbSpecificationExample);

        // 把查询结果转换成vo，并查询每个规格的选项
        List<TbSpecificationVo> rows = tbSpecifications.stream().map(tbSpecification -> {
            TbSpecificationVo tbSpecificationVo = new TbSpecificationVo();
            BeanUtils.copyProperties(tbSpecification, tbSpecificationVo);
            
            // 查询规格选项
            TbSpecificationOptionExample optionExample = new TbSpecificationOptionExample();
            optionExample.createCriteria().andSpecIdEqualTo(tbSpecification.getId());
            List<TbSpecificationOption> options = tbSpecificationOptionMapper.selectByExample(optionExample);
            
            // 转换选项为VO
            List<TbSpecificationOptionVo> optionVos = options.stream().map(opt -> {
                TbSpecificationOptionVo optVo = new TbSpecificationOptionVo();
                BeanUtils.copyProperties(opt, optVo);
                return optVo;
            }).toList();
            
            tbSpecificationVo.setOptions(optionVos);
            return tbSpecificationVo;
        }).toList();

        PageList<TbSpecificationVo> pageList = new PageList<>(page.getTotal(), rows);

        return pageList;
    }

    @Override
    public TbSpecificationVo queryBySpecId(Long specId) {
        // 根据specId查询 tb_specification
        TbSpecification tbSpecification = tbSpecificationMapper.selectByPrimaryKey(specId);
        TbSpecificationVo tbSpecificationVo = new TbSpecificationVo();
        BeanUtils.copyProperties(tbSpecification, tbSpecificationVo);

        // 根据specId查询 tb_specification_option
        TbSpecificationOptionExample tbSpecificationOptionExample = new TbSpecificationOptionExample();
        tbSpecificationOptionExample.createCriteria().andSpecIdEqualTo(specId);
        List<TbSpecificationOption> tbSpecificationOptions = tbSpecificationOptionMapper.selectByExample(tbSpecificationOptionExample);

        List<TbSpecificationOptionVo> list = tbSpecificationOptions.stream().map(tbSpecificationOption -> {
            TbSpecificationOptionVo tbSpecificationOptionVo = new TbSpecificationOptionVo();
            BeanUtils.copyProperties(tbSpecificationOption, tbSpecificationOptionVo);
            return tbSpecificationOptionVo;
        }).toList();

        tbSpecificationVo.setOptions(list);
        return tbSpecificationVo;
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        // 两张表
        tbSpecificationMapper.deleteByPrimaryKey(id);
        TbSpecificationOptionExample tbSpecificationOptionExample = new TbSpecificationOptionExample();
        tbSpecificationOptionExample.createCriteria().andSpecIdEqualTo( id);
        tbSpecificationOptionMapper.deleteByExample(tbSpecificationOptionExample);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        // 两张表
        TbSpecificationExample tbSpecificationExample = new TbSpecificationExample();
        tbSpecificationExample.createCriteria().andIdIn(ids);
        tbSpecificationMapper.deleteByExample( tbSpecificationExample);

        TbSpecificationOptionExample tbSpecificationOptionExample = new TbSpecificationOptionExample();
        tbSpecificationOptionExample.createCriteria().andSpecIdIn(ids);
        tbSpecificationOptionMapper.deleteByExample(tbSpecificationOptionExample);
    }

    @Transactional
    @Override
    public void insert(TbSpecificationDto tbSpecificationDto) {
        // 两张表
        // tb_specification 获取到当前这条数据的自增的id
        TbSpecification tbSpecification = new TbSpecification();
        tbSpecification.setSpecName(tbSpecificationDto.getSpecName());
        tbSpecificationMapper.add(tbSpecification);

        // 添加tb_specification_option
        List<TbSpecificationOption> options = tbSpecificationDto.getOptions();
        for (TbSpecificationOption tbSpecificationOption : options) {
            tbSpecificationOption.setSpecId(tbSpecification.getId());
            tbSpecificationOptionMapper.insert(tbSpecificationOption);
        }
    }

    @Transactional
    @Override
    public void update(TbSpecificationDto tbSpecificationDto) {
        // 1. 修改tb_specification
        TbSpecification tbSpecification = new TbSpecification();
        tbSpecification.setId(tbSpecificationDto.getId());
        tbSpecification.setSpecName(tbSpecificationDto.getSpecName());
        tbSpecificationMapper.updateByPrimaryKeySelective(tbSpecification);

        // 2. 修改tb_specification_option

        // 根据specId 删除options
        TbSpecificationOptionExample tbSpecificationOptionExample = new TbSpecificationOptionExample();
        tbSpecificationOptionExample.createCriteria().andSpecIdEqualTo(tbSpecificationDto.getId());
        tbSpecificationOptionMapper.deleteByExample(tbSpecificationOptionExample);

        // 再添加
        List<TbSpecificationOption> options = tbSpecificationDto.getOptions();
        for (TbSpecificationOption tbSpecificationOption : options) {
            tbSpecificationOption.setSpecId(tbSpecificationDto.getId());
            tbSpecificationOptionMapper.insert(tbSpecificationOption);
        }
    }

    @Override
    public List<TbSpecificationVo> queryAll() {
        List<TbSpecification> tbSpecifications = tbSpecificationMapper.selectByExample(null);
        List<TbSpecificationVo> tbSpecificationVos = tbSpecifications.stream().map(tbSpecification -> {
            TbSpecificationVo tbSpecificationVo = new TbSpecificationVo();
            BeanUtils.copyProperties(tbSpecification, tbSpecificationVo);
            return tbSpecificationVo;
        }).toList();
        return tbSpecificationVos;
    }
}
