package com.cjc.service.impl;

import com.cjc.mapper.TbItemCatMapper;
import com.cjc.mapper.TbTypeTemplateMapper;
import com.cjc.pojo.TbItemCat;
import com.cjc.pojo.TbItemCatExample;
import com.cjc.pojo.TbTypeTemplate;
import com.cjc.service.ItemCatService;
import com.cjc.vo.TbItemCatVo;
import com.cjc.vo.TbTypeTemplateVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemCatServiceImpl implements ItemCatService {

    @Autowired
    private TbItemCatMapper mapper;

    @Autowired
    private TbTypeTemplateMapper tbTypeTemplateMapper;


    // 
    @Override
    public List<Map<String, Object>> queryAll() {
        List<TbItemCat> itemCats = mapper.selectByExample(null);

        List<Map<String, Object>> itemList = getTree(itemCats, 0l);

        return itemList;
    }

    @Override
    public void update(TbItemCatVo itemCatVo) {
        // 计算 parentPath
        calculateParentPath(itemCatVo);
        
        TbItemCat tbItemCat = new TbItemCat();
        BeanUtils.copyProperties(itemCatVo, tbItemCat);
        mapper.updateByPrimaryKeySelective(tbItemCat);
    }

    @Override
    public void add(TbItemCatVo itemCatVo) {
        // 1. 先插入数据（获取自增ID）
        TbItemCat tbItemCat = new TbItemCat();
        BeanUtils.copyProperties(itemCatVo, tbItemCat);
        
        // 处理 parentId
        if (itemCatVo.getParentId() == null) {
            tbItemCat.setParentId(0L);
        }
        
        // 插入数据，ID 会自动回填
        mapper.insertSelective(tbItemCat);
        
        // 2. 计算并更新 parentPath
        itemCatVo.setId(tbItemCat.getId());
        itemCatVo.setParentId(tbItemCat.getParentId());
        calculateParentPath(itemCatVo);
        
        // 3. 更新 parentPath
        TbItemCat updateCat = new TbItemCat();
        updateCat.setId(tbItemCat.getId());
        updateCat.setParentPath(itemCatVo.getParentPath());
        mapper.updateByPrimaryKeySelective(updateCat);
    }

    @Override
    public void delete(Long id) {
        // 检查是否有子分类
        TbItemCatExample example = new TbItemCatExample();
        example.createCriteria().andParentIdEqualTo(id);
        long count = mapper.countByExample(example);
        
        if (count > 0) {
            throw new RuntimeException("该分类下存在子分类，无法删除");
        }
        
        mapper.deleteByPrimaryKey(id);
    }

    /**
     * 计算 parentPath
     * 规则：
     * - 一级分类：/id
     * - 子分类：父分类的parentPath/id
     */
    private void calculateParentPath(TbItemCatVo itemCatVo) {
        if (itemCatVo.getParentId() == null || itemCatVo.getParentId() == 0L) {
            // 一级分类
            itemCatVo.setParentPath("/" + itemCatVo.getId());
            itemCatVo.setParentId(0L);
        } else {
            // 子分类：查询父分类的 parentPath
            TbItemCat parentCat = mapper.selectByPrimaryKey(itemCatVo.getParentId());
            if (parentCat != null) {
                itemCatVo.setParentPath(parentCat.getParentPath() + "/" + itemCatVo.getId());
            }
        }
    }

    @Override
    public TbTypeTemplateVo queryTemplateById(Long id) {
        // 根据catid查询，item_cat
        TbItemCat tbItemCat = mapper.selectByPrimaryKey(id);

        // 根据typeId 查询 tpye_templateT
        TbTypeTemplate tbTypeTemplate = tbTypeTemplateMapper.selectByPrimaryKey(tbItemCat.getTypeId());
        TbTypeTemplateVo typeTemplateVo = new TbTypeTemplateVo();
        BeanUtils.copyProperties(tbTypeTemplate,typeTemplateVo);

        return typeTemplateVo;
    }

    public List<Map<String, Object>> getTree(List<TbItemCat> itemCat, Long parentId) {
        // 定义返回数据结果集
        List<Map<String, Object>> listTree = new ArrayList<>();
        // 遍历菜单
        for (int i = 0; i < itemCat.size(); i++) {
            // 定义map用来存放组装的数据
            Map<String, Object> treeVo = null;
            // 获取单个菜单对象
            TbItemCat item = itemCat.get(i);
            if (item.getParentId().equals(parentId)) { // 如果当前菜单的父id与我们传过来的id一样
                // 实例化map对象
                treeVo = new HashMap<>();
                treeVo.put("id", item.getId());
                treeVo.put("name", item.getName());
                treeVo.put("typeId", item.getTypeId());
                treeVo.put("parentPath", item.getParentPath());
                treeVo.put("parentId", item.getParentId());
                treeVo.put("children", getTree(itemCat, item.getId()));
            }
            // 如果map有数据
            if (treeVo != null) {
                // 将没有子节点的数据删除
                // 1、获取到map的子节点
                List<Map<String, Object>> li = (List<Map<String, Object>>) treeVo.get("children");
                // 2、判断子节点是否有数据
                if (li.size() == 0) {// 表示没有子节点
                    // 将nodes节点删除
                    treeVo.remove("children");
                }
                // 将map放到list中
                listTree.add(treeVo);
            }
        }
        return listTree;
    }
}
