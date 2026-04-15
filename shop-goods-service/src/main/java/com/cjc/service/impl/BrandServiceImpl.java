package com.cjc.service.impl;

import com.cjc.exception.BusinessException;
import com.cjc.mapper.TbBrandMapper;
import com.cjc.pojo.TbBrand;
import com.cjc.pojo.TbBrandExample;
import com.cjc.query.QueryParams;
import com.cjc.service.BrandService;
import com.cjc.util.PageList;
import com.cjc.vo.TbBrandVo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private TbBrandMapper tbBrandMapper;

    @Override
    public PageList<TbBrandVo> queryPage(QueryParams<TbBrand> queryParams) {
        // 设置分页参数
        Page<TbBrand> objects = PageHelper.startPage(queryParams.getCurrentPage(), queryParams.getPageSize());
        // 查询所有
        // 根据名称查询
        TbBrandExample tbBrandExample = new TbBrandExample();
        if (queryParams.getParams() != null && queryParams.getParams().getName() != null && queryParams.getParams().getName().trim().length() > 0)  {
            tbBrandExample.createCriteria().andNameLike("%" + queryParams.getParams().getName() + "%");
        }
        List<TbBrand> tbBrands = tbBrandMapper.selectByExample(tbBrandExample);
        // tbBrands -- > vo
        List<TbBrandVo> tbBrandVos = tbBrands.stream().map(tbBrand -> {
            TbBrandVo tbBrandVo = new TbBrandVo();
            // 方法：拷贝属性的
            BeanUtils.copyProperties(tbBrand, tbBrandVo);
            return tbBrandVo;
        }).collect(Collectors.toList());
        PageList<TbBrandVo> pageList = new PageList<>(objects.getTotal(), tbBrandVos);
        return pageList;
    }

    @Override
    public void insert(TbBrand tbBrand) {
        // 添加
        tbBrandMapper.insert(tbBrand);
    }

    @Override
    public void update(TbBrand tbBrand) {
        // 修改操作
        tbBrandMapper.updateByPrimaryKeySelective(tbBrand);
    }

    @Override
    public void delete(Long id) {

        // 删除操作
        tbBrandMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void batchDelete(List<Long> ids) {
        // 批量删除操作
        tbBrandMapper.batchDelete(ids);
    }

    @Override
    public List<TbBrandVo> queryAll() {
        List<TbBrand> tbBrands = tbBrandMapper.selectByExample(null);
        List<TbBrandVo> tbBrandVos = tbBrands.stream().map(tbBrand -> {
            TbBrandVo tbBrandVo = new TbBrandVo();
            BeanUtils.copyProperties(tbBrand, tbBrandVo);
            return tbBrandVo;
        }).collect(Collectors.toList());
//        if(true){
//            throw new BusinessException("密码错误");
//        }
        return tbBrandVos;
    }
}
