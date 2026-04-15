package com.cjc.service.impl;

import com.cjc.mapper.TbContentCategoryMapper;
import com.cjc.mapper.TbContentMapper;
import com.cjc.pojo.TbContent;
import com.cjc.pojo.TbContentCategory;
import com.cjc.pojo.TbContentCategoryExample;
import com.cjc.pojo.TbContentExample;
import com.cjc.query.QueryParams;
import com.cjc.service.ContentService;
import com.cjc.util.PageList;
import com.cjc.vo.TbContentCategoryVo;
import com.cjc.vo.TbContentVo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private TbContentMapper contentMapper;

    @Autowired
    private TbContentCategoryMapper categoryMapper;

    @Override
    public PageList<TbContentVo> queryPage(QueryParams<TbContent> queryParams) {
        Page<TbContent> page = PageHelper.startPage(queryParams.getCurrentPage(), queryParams.getPageSize());
        
        TbContentExample example = new TbContentExample();
        example.setOrderByClause("sort_order asc, id desc");
        
        // 条件查询
        if (queryParams.getParams() != null) {
            TbContent params = queryParams.getParams();
            TbContentExample.Criteria criteria = example.createCriteria();
            
            if (params.getTitle() != null && params.getTitle().trim().length() > 0) {
                criteria.andTitleLike("%" + params.getTitle() + "%");
            }
            if (params.getCategoryId() != null) {
                criteria.andCategoryIdEqualTo(params.getCategoryId());
            }
            if (params.getStatus() != null && params.getStatus().trim().length() > 0) {
                criteria.andStatusEqualTo(params.getStatus());
            }
        }
        
        List<TbContent> list = contentMapper.selectByExample(example);
        
        List<TbContentVo> voList = list.stream().map(content -> {
            TbContentVo vo = new TbContentVo();
            BeanUtils.copyProperties(content, vo);
            return vo;
        }).collect(Collectors.toList());
        
        return new PageList<>(page.getTotal(), voList);
    }

    @Override
    public void insert(TbContent content) {
        contentMapper.insertSelective(content);
    }

    @Override
    public void update(TbContent content) {
        contentMapper.updateByPrimaryKeySelective(content);
    }

    @Override
    public void delete(Long id) {
        contentMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void batchDelete(List<Long> ids) {
        contentMapper.batchDelete(ids);
    }

    @Override
    public List<TbContentVo> queryAll() {
        TbContentExample example = new TbContentExample();
        example.setOrderByClause("sort_order asc, id desc");
        List<TbContent> list = contentMapper.selectByExample(example);
        
        return list.stream().map(content -> {
            TbContentVo vo = new TbContentVo();
            BeanUtils.copyProperties(content, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<TbContentVo> queryByCategoryId(Long categoryId) {
        TbContentExample example = new TbContentExample();
        example.createCriteria().andCategoryIdEqualTo(categoryId);
        example.setOrderByClause("sort_order asc, id desc");
        List<TbContent> list = contentMapper.selectByExample(example);
        
        return list.stream().map(content -> {
            TbContentVo vo = new TbContentVo();
            BeanUtils.copyProperties(content, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<TbContentVo> queryByCategoryKey(String key) {
        // 先根据KEY找到分类
        TbContentCategoryExample catExample = new TbContentCategoryExample();
        catExample.createCriteria().andKeyEqualTo(key);
        List<TbContentCategory> categories = categoryMapper.selectByExample(catExample);
        
        if (categories == null || categories.isEmpty()) {
            return List.of();
        }
        
        Long categoryId = categories.get(0).getId();
        return queryByCategoryId(categoryId);
    }

    @Override
    public PageList<TbContentCategoryVo> queryCategoryPage(QueryParams<TbContentCategory> queryParams) {
        Page<TbContentCategory> page = PageHelper.startPage(queryParams.getCurrentPage(), queryParams.getPageSize());
        
        TbContentCategoryExample example = new TbContentCategoryExample();
        example.setOrderByClause("id asc");
        
        if (queryParams.getParams() != null) {
            TbContentCategory params = queryParams.getParams();
            TbContentCategoryExample.Criteria criteria = example.createCriteria();
            
            if (params.getName() != null && params.getName().trim().length() > 0) {
                criteria.andNameLike("%" + params.getName() + "%");
            }
        }
        
        List<TbContentCategory> list = categoryMapper.selectByExample(example);
        
        List<TbContentCategoryVo> voList = list.stream().map(category -> {
            TbContentCategoryVo vo = new TbContentCategoryVo();
            BeanUtils.copyProperties(category, vo);
            return vo;
        }).collect(Collectors.toList());
        
        return new PageList<>(page.getTotal(), voList);
    }

    @Override
    public void insertCategory(TbContentCategory category) {
        categoryMapper.insertSelective(category);
    }

    @Override
    public void updateCategory(TbContentCategory category) {
        categoryMapper.updateByPrimaryKeySelective(category);
    }

    @Override
    public void deleteCategory(Long id) {
        categoryMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<TbContentCategoryVo> queryAllCategory() {
        TbContentCategoryExample example = new TbContentCategoryExample();
        example.setOrderByClause("id asc");
        List<TbContentCategory> list = categoryMapper.selectByExample(example);
        
        return list.stream().map(category -> {
            TbContentCategoryVo vo = new TbContentCategoryVo();
            BeanUtils.copyProperties(category, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}