package com.cjc.service.impl;

import com.cjc.dto.GoodsDto;
import com.cjc.dto.TbItemDto;
import com.cjc.exception.BusinessException;
import com.cjc.mapper.TbGoodsDescMapper;
import com.cjc.mapper.TbGoodsMapper;
import com.cjc.mapper.TbItemCatMapper;
import com.cjc.mapper.TbItemMapper;
import com.cjc.mapper.TbSpecificationMapper;
import com.cjc.mapper.TbSpecificationOptionMapper;
import com.cjc.mapper.TbTypeTemplateMapper;
import com.cjc.pojo.*;
import com.cjc.query.QueryParams;
import com.cjc.service.GoodsService;
import com.cjc.service.GoodsStaticService;
import com.cjc.util.AliOssUtil;
import com.cjc.util.PageList;
import com.cjc.vo.TbGoodsVo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;
    
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    
    @Autowired
    private AliOssUtil aliOssUtil;
    
    @Autowired
    private TbItemCatMapper itemCatMapper;
    
    @Autowired
    private TbTypeTemplateMapper typeTemplateMapper;
    
    @Autowired
    private TbSpecificationMapper specificationMapper;
    
    @Autowired
    private TbSpecificationOptionMapper specificationOptionMapper;
    
    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private GoodsStaticService goodsStaticService;

    @Override
    @Transactional
    public void save(GoodsDto dto, String sellerId) {
        // 创建商品实体（主表）
        TbGoods goods = new TbGoods();
        // 基础信息
        goods.setSellerId(sellerId);
        goods.setGoodsName(dto.getGoodsName());
        goods.setCaption(dto.getSubTitle());  // 副标题
        goods.setPrice(dto.getPrice());
        goods.setSmallPic(dto.getImage());    // 商品主图
        // 品牌
        goods.setBrandId(dto.getBrandId());
        // 分类（拆分数组）
        List<Long> categoryIds = dto.getCategoryId();
        Long category3Id = null;
        if (categoryIds != null && categoryIds.size() > 0) {
            goods.setCategory1Id(categoryIds.get(0));
            if (categoryIds.size() > 1) {
                goods.setCategory2Id(categoryIds.get(1));
            }
            if (categoryIds.size() > 2) {
                category3Id = categoryIds.get(2);
                goods.setCategory3Id(category3Id);
            }
        }
        // 是否启用规格
        goods.setIsEnableSpec(dto.getIsEnableSpec() != null ? dto.getIsEnableSpec() : "0");
        // 默认值
        goods.setAuditStatus("0");      // 0=未提交
        goods.setIsMarketable("0");     // 0=下架（未审核通过前不能上架）
        goods.setIsDelete("0");         // 0=未删除
        // 插入主表（tb_goods）
        goodsMapper.insertSelective(goods);
        
        // ========== 同时插入 tb_goods_desc 扩展表 ==========
        TbGoodsDesc goodsDesc = new TbGoodsDesc();
        goodsDesc.setGoodsId(goods.getId());  // 使用刚插入的商品ID
        goodsDesc.setIntroduction(dto.getIntroduction());   // 富文本内容
        goodsDesc.setPackageList(dto.getPackageList());     // 包装清单
        goodsDesc.setSaleService(dto.getAfterService());    // 售后服务
        // 规格选项和扩展属性
        goodsDesc.setSpecificationItems(dto.getSpecificationItems());
        goodsDesc.setCustomAttributeItems(dto.getCustomAttributeItems());
        // 插入扩展表
        goodsDescMapper.insertSelective(goodsDesc);
        
        // ========== 插入 tb_item（SKU列表）==========
        List<TbItemDto> itemList = dto.getItemList();
        log.info("========== SKU保存调试 ========== isEnableSpec={}, itemList数量={}",
            dto.getIsEnableSpec(), itemList != null ? itemList.size() : "null");

        // 获取分类名称（用于 SKU 标题）
        String categoryName = "";
        if (category3Id != null) {
            TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(category3Id);
            if (itemCat != null) {
                categoryName = itemCat.getName();
            }
        }

        Long defaultItemId = null;

        // 多规格模式：处理itemList
        if (dto.getIsEnableSpec() != null && dto.getIsEnableSpec().equals("1") && itemList != null && itemList.size() > 0) {
            log.info("itemList内容: {}", JSON.toJSONString(itemList));
            List<TbItem> items = new ArrayList<>();

            for (TbItemDto itemDto : itemList) {
                TbItem item = new TbItem();

                // SKU 标题：商品名 + 规格组合
                String specStr = itemDto.getSpec();
                String title = dto.getGoodsName();
                if (specStr != null && !specStr.isEmpty()) {
                    // 解析规格 JSON，拼接成标题
                    try {
                        JSONObject specJson = JSON.parseObject(specStr);
                        StringBuilder sb = new StringBuilder(dto.getGoodsName());
                        for (String key : specJson.keySet()) {
                            sb.append(" ").append(specJson.getString(key));
                        }
                        title = sb.toString();
                    } catch (Exception e) {
                        log.warn("解析规格JSON失败: {}", specStr);
                    }
                }
                item.setTitle(title);
                
                // 基本信息
                item.setGoodsId(goods.getId());
                item.setSellerId(sellerId);
                item.setCategoryid(category3Id);
                item.setCategory(categoryName);
                item.setBrand(dto.getBrandId() != null ? dto.getBrandId().toString() : "");
                
                // 图片
                item.setImage(dto.getImage());
                item.setCartThumbnail(dto.getImage());
                
                // 价格和库存
                item.setPrice(itemDto.getPrice());
                item.setNum(itemDto.getNum());
                item.setStockCount(itemDto.getNum());
                
                // 规格JSON
                item.setSpec(specStr);
                
                // 卖点
                item.setSellPoint(dto.getSubTitle());
                
                // 状态
                item.setStatus(itemDto.getEnabled() != null && itemDto.getEnabled() ? "1" : "0");
                
                // 是否默认
                String isDefault = itemDto.getDefaultFlag() != null && itemDto.getDefaultFlag() ? "1" : "0";
                item.setIsDefault(isDefault);
                
                // 时间
                item.setCreateTime(new Date());
                item.setUpdateTime(new Date());
                
                // 记录默认 SKU 的 ID
                if ("1".equals(isDefault)) {
                    defaultItemId = item.getId();  // 注意：此时 id 还是 null，插入后才有
                }
                
                items.add(item);
            }
            
            // 批量插入 SKU（注意：需要在 TbItemMapper.xml 中添加 insertBatch 方法）
            // 如果没有 insertBatch，逐个插入
            for (TbItem item : items) {
                itemMapper.insertSelective(item);
                // 插入后获取 ID，如果是默认的则记录
                if ("1".equals(item.getIsDefault())) {
                    defaultItemId = item.getId();
                }
            }
            
            // 更新商品的 default_item_id
            if (defaultItemId != null) {
                goods.setDefaultItemId(defaultItemId);
                goodsMapper.updateByPrimaryKeySelective(goods);
            }
            
            log.info("商品SKU保存成功，goodsId={}, SKU数量={}", goods.getId(), items.size());
        } else {
            // 单品模式：创建一个默认的 SKU 记录，存储库存
            TbItem defaultItem = new TbItem();
            defaultItem.setTitle(dto.getGoodsName());  // SKU标题=商品名称
            defaultItem.setGoodsId(goods.getId());
            defaultItem.setSellerId(sellerId);
            defaultItem.setCategoryid(category3Id);
            defaultItem.setCategory(categoryName);
            defaultItem.setBrand(dto.getBrandId() != null ? dto.getBrandId().toString() : "");
            defaultItem.setImage(dto.getImage());
            defaultItem.setCartThumbnail(dto.getImage());

            // 单品模式：价格使用商品主表价格，库存从itemList获取或使用默认值
            defaultItem.setPrice(dto.getPrice());  // 使用商品主表价格

            // 库存：优先从 itemList 获取，否则使用默认值 99
            Integer stockCount = 99;  // 默认库存
            if (itemList != null && itemList.size() > 0 && itemList.get(0).getNum() != null && itemList.get(0).getNum() > 0) {
                stockCount = itemList.get(0).getNum();
            }
            defaultItem.setNum(stockCount);
            defaultItem.setStockCount(stockCount);

            defaultItem.setSpec("{}");  // 单品无规格
            defaultItem.setSellPoint(dto.getSubTitle());
            defaultItem.setStatus("1");  // 默认启用
            defaultItem.setIsDefault("1");  // 单品模式下这个是默认SKU
            defaultItem.setCreateTime(new Date());
            defaultItem.setUpdateTime(new Date());

            itemMapper.insertSelective(defaultItem);
            defaultItemId = defaultItem.getId();

            // 更新商品的 default_item_id
            goods.setDefaultItemId(defaultItemId);
            TbGoods updateGoods = new TbGoods();
            updateGoods.setId(goods.getId());
            updateGoods.setDefaultItemId(defaultItemId);
            goodsMapper.updateByPrimaryKeySelective(updateGoods);

            log.info("单品SKU保存成功，goodsId={}, itemId={}, price={}, stockCount={}",
                goods.getId(), defaultItemId, dto.getPrice(), stockCount);
        }
        
        log.info("商品保存成功，goodsId={}", goods.getId());
    }

    @Override
    public PageList<TbGoodsVo> queryPage(QueryParams<TbGoods> queryParams, String sellerId) {
        PageHelper.startPage(queryParams.getCurrentPage(), queryParams.getPageSize());
        
        // 使用关联查询，获取分类名称
        String goodsName = null;
        String auditStatus = null;
        if (queryParams.getParams() != null) {
            goodsName = queryParams.getParams().getGoodsName();
            auditStatus = queryParams.getParams().getAuditStatus();
        }
        
        List<TbGoodsVo> list = goodsMapper.selectGoodsVoList(sellerId, goodsName, auditStatus);
        
        // 获取总数
        long total = ((Page<?>) list).getTotal();
        
        return new PageList<>(total, list);
    }

    @Override
    @Transactional
    public void delete(Long id, String sellerId) {
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        if (tbGoods == null) {
            throw new BusinessException("商品不存在！");
        }
        
        // 核心安校验：判断这件商品到底是不是当前登录商家的
        if (!tbGoods.getSellerId().equals(sellerId)) {
            throw new BusinessException("非法操作：您无权删除其他商家的商品！");
        }
        
        // ========== 删除 OSS 图片资源 ==========
        // 1. 删除商品主图
        if (tbGoods.getSmallPic() != null && !tbGoods.getSmallPic().isEmpty()) {
            aliOssUtil.delete(tbGoods.getSmallPic());
            log.info("删除商品主图: {}", tbGoods.getSmallPic());
        }
        
        // 2. 查询 tb_goods_desc，删除富文本中的图片
        TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        if (goodsDesc != null && goodsDesc.getIntroduction() != null) {
            List<String> imageUrls = aliOssUtil.extractImageUrls(goodsDesc.getIntroduction());
            if (!imageUrls.isEmpty()) {
                aliOssUtil.deleteBatch(imageUrls);
                log.info("删除富文本图片，共 {} 张", imageUrls.size());
            }
            // 3. 删除 tb_goods_desc 记录
            goodsDescMapper.deleteByPrimaryKey(id);
        }

        // 4. 逻辑删除主表记录
        TbGoods updateGoods = new TbGoods();
        updateGoods.setId(id);
        updateGoods.setIsDelete("1");           // 1 表示已删除
        updateGoods.setIsMarketable("0");       // 顺便把状态改成下架，防止前台还能搜到
        goodsMapper.updateByPrimaryKeySelective(updateGoods);
        
        log.info("商品删除完成（逻辑删除），goodsId={}", id);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids, String sellerId) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        
        // ========== 批量删除 OSS 图片资源 ==========
        for (Long id : ids) {
            TbGoods goods = goodsMapper.selectByPrimaryKey(id);
            if (goods == null || !goods.getSellerId().equals(sellerId)) {
                continue;  // 跳过不存在或无权限的商品
            }
            
            // 删除商品主图
            if (goods.getSmallPic() != null && !goods.getSmallPic().isEmpty()) {
                aliOssUtil.delete(goods.getSmallPic());
            }
            
            // 删除富文本中的图片
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);
            if (goodsDesc != null && goodsDesc.getIntroduction() != null) {
                List<String> imageUrls = aliOssUtil.extractImageUrls(goodsDesc.getIntroduction());
                if (!imageUrls.isEmpty()) {
                    aliOssUtil.deleteBatch(imageUrls);
                }
                // 删除 tb_goods_desc 记录
                goodsDescMapper.deleteByPrimaryKey(id);
            }
        }
        
        // 执行批量逻辑删除
        goodsMapper.logicalBatchDelete(ids, sellerId);
        log.info("批量删除商品完成，共 {} 个，OSS 图片已清理", ids.size());
    }

    @Override
    public GoodsDto getById(Long id, String sellerId) {
        TbGoods goods = goodsMapper.selectByPrimaryKey(id);
        if (goods == null) {
            throw new BusinessException("商品不存在！");
        }

        // 安全校验：防止越权访问
        if (!goods.getSellerId().equals(sellerId)) {
            throw new BusinessException("非法操作：您无权查看其他商家的商品！");
        }

        // 组装 DTO（主表数据）
        GoodsDto dto = new GoodsDto();
        dto.setId(goods.getId());
        dto.setGoodsName(goods.getGoodsName());
        dto.setSubTitle(goods.getCaption());
        dto.setPrice(goods.getPrice());
        dto.setBrandId(goods.getBrandId());
        dto.setImage(goods.getSmallPic());
        dto.setIsEnableSpec(goods.getIsEnableSpec());  // 是否启用规格

        // 关键：组装 categoryId 数组，供前端级联选择器回显
        List<Long> categoryIds = new ArrayList<>();
        if (goods.getCategory1Id() != null) {
            categoryIds.add(goods.getCategory1Id());
        }
        if (goods.getCategory2Id() != null) {
            categoryIds.add(goods.getCategory2Id());
        }
        if (goods.getCategory3Id() != null) {
            categoryIds.add(goods.getCategory3Id());
        }
        dto.setCategoryId(categoryIds);

        // ========== 查询 tb_goods_desc 扩展表 ==========
        TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        if (goodsDesc != null) {
            dto.setIntroduction(goodsDesc.getIntroduction());   // 富文本内容
            dto.setPackageList(goodsDesc.getPackageList());     // 包装清单
            dto.setAfterService(goodsDesc.getSaleService());    // 售后服务（注意字段名映射）
            dto.setItemImages(goodsDesc.getItemImages());       // 商品图片列表
            dto.setSpecificationItems(goodsDesc.getSpecificationItems());  // 规格选项
            dto.setCustomAttributeItems(goodsDesc.getCustomAttributeItems()); // 扩展属性
        }

        // ========== 查询 SKU 列表 ==========
        TbItemExample itemExample = new TbItemExample();
        itemExample.createCriteria().andGoodsIdEqualTo(id);
        List<TbItem> items = itemMapper.selectByExample(itemExample);

        if (items != null && !items.isEmpty()) {
            List<TbItemDto> itemDtos = new ArrayList<>();
            for (TbItem item : items) {
                TbItemDto itemDto = new TbItemDto();
                itemDto.setSpec(item.getSpec());
                itemDto.setPrice(item.getPrice());
                itemDto.setNum(item.getStockCount());
                itemDto.setEnabled("1".equals(item.getStatus()));
                itemDto.setDefaultFlag("1".equals(item.getIsDefault()));
                itemDtos.add(itemDto);
            }
            dto.setItemList(itemDtos);
        }

        return dto;
    }

    @Override
    @Transactional
    public void update(GoodsDto dto, String sellerId) {
        TbGoods goods = goodsMapper.selectByPrimaryKey(dto.getId());
        if (goods == null) {
            throw new BusinessException("商品不存在！");
        }

        // 安全校验：防止越权修改
        if (!goods.getSellerId().equals(sellerId)) {
            throw new BusinessException("非法操作：您无权修改其他商家的商品！");
        }

        // 更新商品主表信息
        TbGoods updateGoods = new TbGoods();
        updateGoods.setId(dto.getId());
        updateGoods.setGoodsName(dto.getGoodsName());
        updateGoods.setCaption(dto.getSubTitle());
        updateGoods.setPrice(dto.getPrice());
        updateGoods.setBrandId(dto.getBrandId());
        updateGoods.setSmallPic(dto.getImage());  // 商品主图

        // 分类（拆分数组）
        List<Long> categoryIds = dto.getCategoryId();
        if (categoryIds != null && categoryIds.size() > 0) {
            updateGoods.setCategory1Id(categoryIds.get(0));
            if (categoryIds.size() > 1) {
                updateGoods.setCategory2Id(categoryIds.get(1));
            }
            if (categoryIds.size() > 2) {
                updateGoods.setCategory3Id(categoryIds.get(2));
            }
        }

        // 更新是否启用规格
        updateGoods.setIsEnableSpec(dto.getIsEnableSpec() != null ? dto.getIsEnableSpec() : "0");

        // 业务铁律：修改后强制重置为未审核状态，并下架
        updateGoods.setAuditStatus("0");
        updateGoods.setIsMarketable("0");  // 修改后需要重新审核，先下架

        goodsMapper.updateByPrimaryKeySelective(updateGoods);

        // ========== 同时更新 tb_goods_desc 扩展表 ==========
        TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(dto.getId());
        if (goodsDesc != null) {
            // 存在则更新
            TbGoodsDesc updateDesc = new TbGoodsDesc();
            updateDesc.setGoodsId(dto.getId());
            updateDesc.setIntroduction(dto.getIntroduction());   // 富文本内容
            updateDesc.setPackageList(dto.getPackageList());     // 包装清单
            updateDesc.setSaleService(dto.getAfterService());    // 售后服务
            // 更新规格选项和扩展属性
            updateDesc.setSpecificationItems(dto.getSpecificationItems());
            updateDesc.setCustomAttributeItems(dto.getCustomAttributeItems());
            goodsDescMapper.updateByPrimaryKeySelective(updateDesc);
        } else {
            // 不存在则插入（兼容旧数据）
            TbGoodsDesc newDesc = new TbGoodsDesc();
            newDesc.setGoodsId(dto.getId());
            newDesc.setIntroduction(dto.getIntroduction());
            newDesc.setPackageList(dto.getPackageList());
            newDesc.setSaleService(dto.getAfterService());
            newDesc.setSpecificationItems(dto.getSpecificationItems());
            newDesc.setCustomAttributeItems(dto.getCustomAttributeItems());
            goodsDescMapper.insertSelective(newDesc);
        }

        // ========== 更新 SKU 数据（关键修复！）==========
        // 1. 删除旧的 SKU 数据
        TbItemExample deleteExample = new TbItemExample();
        deleteExample.createCriteria().andGoodsIdEqualTo(dto.getId());
        itemMapper.deleteByExample(deleteExample);
        log.info("删除旧SKU数据，goodsId={}", dto.getId());

        // 2. 如果是多规格模式，重新插入 SKU
        List<TbItemDto> itemList = dto.getItemList();
        if (dto.getIsEnableSpec() != null && dto.getIsEnableSpec().equals("1") && itemList != null && itemList.size() > 0) {
            Long category3Id = categoryIds != null && categoryIds.size() > 2 ? categoryIds.get(2) : null;

            // 获取分类名称（用于 SKU 标题）
            String categoryName = "";
            if (category3Id != null) {
                TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(category3Id);
                if (itemCat != null) {
                    categoryName = itemCat.getName();
                }
            }

            Long defaultItemId = null;
            List<TbItem> items = new ArrayList<>();

            for (TbItemDto itemDto : itemList) {
                TbItem item = new TbItem();

                // SKU 标题：商品名 + 规格组合
                String specStr = itemDto.getSpec();
                String title = dto.getGoodsName();
                if (specStr != null && !specStr.isEmpty()) {
                    try {
                        JSONObject specJson = JSON.parseObject(specStr);
                        StringBuilder sb = new StringBuilder(dto.getGoodsName());
                        for (String key : specJson.keySet()) {
                            sb.append(" ").append(specJson.getString(key));
                        }
                        title = sb.toString();
                    } catch (Exception e) {
                        log.warn("解析规格JSON失败: {}", specStr);
                    }
                }
                item.setTitle(title);

                // 基本信息
                item.setGoodsId(dto.getId());
                item.setSellerId(sellerId);
                item.setCategoryid(category3Id);
                item.setCategory(categoryName);
                item.setBrand(dto.getBrandId() != null ? dto.getBrandId().toString() : "");

                // 图片
                item.setImage(dto.getImage());
                item.setCartThumbnail(dto.getImage());

                // 价格和库存
                item.setPrice(itemDto.getPrice());
                item.setNum(itemDto.getNum());
                item.setStockCount(itemDto.getNum());

                // 规格JSON
                item.setSpec(specStr);

                // 卖点
                item.setSellPoint(dto.getSubTitle());

                // 状态
                item.setStatus(itemDto.getEnabled() != null && itemDto.getEnabled() ? "1" : "0");

                // 是否默认
                String isDefault = itemDto.getDefaultFlag() != null && itemDto.getDefaultFlag() ? "1" : "0";
                item.setIsDefault(isDefault);

                // 时间
                item.setCreateTime(new Date());
                item.setUpdateTime(new Date());

                items.add(item);
            }

            // 批量插入 SKU
            for (TbItem item : items) {
                itemMapper.insertSelective(item);
                // 插入后获取 ID，如果是默认的则记录
                if ("1".equals(item.getIsDefault())) {
                    defaultItemId = item.getId();
                }
            }

            // 更新商品的 default_item_id
            if (defaultItemId != null) {
                TbGoods updateDefault = new TbGoods();
                updateDefault.setId(dto.getId());
                updateDefault.setDefaultItemId(defaultItemId);
                goodsMapper.updateByPrimaryKeySelective(updateDefault);
            }

            log.info("商品SKU更新完成，goodsId={}, SKU数量={}", dto.getId(), items.size());
        } else {
            // 单品模式：创建一个默认的 SKU 记录，存储库存
            TbItem defaultItem = new TbItem();
            defaultItem.setTitle(dto.getGoodsName());  // SKU标题=商品名称
            defaultItem.setGoodsId(dto.getId());
            defaultItem.setSellerId(sellerId);

            // 获取三级分类ID和名称
            Long category3Id = categoryIds != null && categoryIds.size() > 2 ? categoryIds.get(2) : null;
            String categoryName = "";
            if (category3Id != null) {
                TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(category3Id);
                if (itemCat != null) {
                    categoryName = itemCat.getName();
                }
            }
            defaultItem.setCategoryid(category3Id);
            defaultItem.setCategory(categoryName);
            defaultItem.setBrand(dto.getBrandId() != null ? dto.getBrandId().toString() : "");
            defaultItem.setImage(dto.getImage());
            defaultItem.setCartThumbnail(dto.getImage());

            // 单品模式：价格使用商品主表价格，库存从itemList获取或使用默认值
            defaultItem.setPrice(dto.getPrice());  // 使用商品主表价格

            // 库存：优先从 itemList 获取，否则使用默认值 99
            Integer stockCount = 99;  // 默认库存
            if (itemList != null && itemList.size() > 0 && itemList.get(0).getNum() != null && itemList.get(0).getNum() > 0) {
                stockCount = itemList.get(0).getNum();
            }
            defaultItem.setNum(stockCount);
            defaultItem.setStockCount(stockCount);

            defaultItem.setSpec("{}");  // 单品无规格
            defaultItem.setSellPoint(dto.getSubTitle());
            defaultItem.setStatus("1");  // 默认启用
            defaultItem.setIsDefault("1");  // 单品模式下这个是默认SKU
            defaultItem.setCreateTime(new Date());
            defaultItem.setUpdateTime(new Date());

            itemMapper.insertSelective(defaultItem);

            // 更新商品的 default_item_id
            TbGoods updateDefault = new TbGoods();
            updateDefault.setId(dto.getId());
            updateDefault.setDefaultItemId(defaultItem.getId());
            goodsMapper.updateByPrimaryKeySelective(updateDefault);
            log.info("单品模式SKU创建成功，goodsId={}, itemId={}, price={}, stock={}",
                dto.getId(), defaultItem.getId(), dto.getPrice(), stockCount);
        }

        log.info("商品更新完成，goodsId={}", dto.getId());
    }

    @Override
    @Transactional
    public void submitAudit(List<Long> ids, String sellerId) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 批量提交审核，只有未审核(0)和已驳回(3)的商品才能提交
        goodsMapper.submitAudit(ids, sellerId);
    }

    @Override
    @Transactional
    public void putOnSale(List<Long> ids, String sellerId) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 只有审核通过(2)的商品才能上架
        goodsMapper.putOnSale(ids, sellerId);

        // 上架时生成静态页并更新数据库
        for (Long id : ids) {
            try {
                String staticUrl = goodsStaticService.generateStaticPage(id);
                TbGoods updateGoods = new TbGoods();
                updateGoods.setId(id);
                updateGoods.setStaticUrl(staticUrl);
                goodsMapper.updateByPrimaryKeySelective(updateGoods);
                log.info("上架成功，静态页生成成功: goodsId={}", id);
            } catch (Exception e) {
                log.error("静态页生成失败: goodsId={}", id, e);
            }
        }
    }

    @Override
    @Transactional
    public void pullOffSale(List<Long> ids, String sellerId) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 批量查询商品信息（优化：避免N+1查询）
        TbGoodsExample example = new TbGoodsExample();
        example.createCriteria().andIdIn(ids);
        List<TbGoods> goodsList = goodsMapper.selectByExample(example);

        List<Long> needDeleteIds = new ArrayList<>();
        Map<Long, String> staticUrlMap = new HashMap<>();

        for (TbGoods goods : goodsList) {
            if (goods.getStaticUrl() != null && !goods.getStaticUrl().isEmpty()) {
                needDeleteIds.add(goods.getId());
                staticUrlMap.put(goods.getId(), goods.getStaticUrl());
            }
        }

        // 批量删除静态页
        for (Map.Entry<Long, String> entry : staticUrlMap.entrySet()) {
            try {
                goodsStaticService.deleteStaticPage(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                log.warn("删除静态页失败: goodsId={}", entry.getKey(), e);
            }
        }

        // 批量清空静态页URL（优化：批量更新）
        if (!needDeleteIds.isEmpty()) {
            goodsMapper.batchClearStaticUrl(needDeleteIds);
        }

        // 下架商品
        goodsMapper.pullOffSale(ids, sellerId);
    }

    // ========== 运营商接口实现 ==========

    @Override
    public PageList<TbGoodsVo> adminQueryPage(QueryParams<TbGoods> queryParams) {
        PageHelper.startPage(queryParams.getCurrentPage(), queryParams.getPageSize());
        
        // 运营商查询所有商品，不传 sellerId
        String goodsName = null;
        String auditStatus = null;
        if (queryParams.getParams() != null) {
            goodsName = queryParams.getParams().getGoodsName();
            auditStatus = queryParams.getParams().getAuditStatus();
        }
        
        // 传 null 作为 sellerId，查询所有商家的商品
        List<TbGoodsVo> list = goodsMapper.selectGoodsVoList(null, goodsName, auditStatus);
        
        long total = ((Page<?>) list).getTotal();
        
        return new PageList<>(total, list);
    }

    @Override
    @Transactional
    public void audit(Long id, String auditStatus) {
        TbGoods goods = goodsMapper.selectByPrimaryKey(id);
        if (goods == null) {
            throw new BusinessException("商品不存在！");
        }

        // 校验：只能审核待审核(1)状态的商品
        if (!"1".equals(goods.getAuditStatus())) {
            throw new BusinessException("只能审核待审核状态的商品！");
        }

        // 更新审核状态
        // 状态定义：0=未提交, 1=待审核, 2=审核通过, 3=审核驳回
        // 前端传 auditStatus='2' 表示通过，'3' 表示驳回（直接传目标状态）
        TbGoods updateGoods = new TbGoods();
        updateGoods.setId(id);
        updateGoods.setAuditStatus(auditStatus);  // 直接使用前端传的目标状态

        if ("2".equals(auditStatus)) {
            // 审核通过，自动上架
            updateGoods.setIsMarketable("1");

            // ⭐ 触发静态页生成（以空间换时间）
            try {
                String staticUrl = goodsStaticService.generateStaticPage(id);
                updateGoods.setStaticUrl(staticUrl);  // 保存静态页URL
                log.info("审核通过，静态页生成成功: goodsId={}", id);
            } catch (Exception e) {
                log.error("静态页生成失败，但不影响审核流程: goodsId={}", id, e);
                // 静态化失败不影响审核，用户仍可通过动态接口访问
            }
        }
        // 审核驳回不需要改 isMarketable，保持原样

        goodsMapper.updateByPrimaryKeySelective(updateGoods);
    }

    @Override
    @Transactional
    public void batchAudit(List<Long> ids, String auditStatus) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 批量审核，只更新待审核状态的商品
        goodsMapper.adminBatchAudit(ids, auditStatus);

        // 审核通过时批量生成静态页并更新数据库
        if ("2".equals(auditStatus)) {
            for (Long id : ids) {
                try {
                    String staticUrl = goodsStaticService.generateStaticPage(id);
                    // 更新数据库中的staticUrl
                    TbGoods updateGoods = new TbGoods();
                    updateGoods.setId(id);
                    updateGoods.setStaticUrl(staticUrl);
                    goodsMapper.updateByPrimaryKeySelective(updateGoods);
                    log.info("审核通过，静态页生成成功: goodsId={}", id);
                } catch (Exception e) {
                    log.error("静态页生成失败: goodsId={}", id, e);
                }
            }
        }
    }

    @Override
    @Transactional
    public void adminBatchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 运营商批量逻辑删除
        goodsMapper.adminBatchDelete(ids);
    }

    // ========== 前台公开接口实现 ==========

    @Override
    public PageList<TbGoodsVo> list(QueryParams<TbGoods> queryParams) {
        // 处理 null 值，设置默认值
        Integer currentPage = queryParams.getCurrentPage() != null ? queryParams.getCurrentPage() : 1;
        Integer pageSize = queryParams.getPageSize() != null ? queryParams.getPageSize() : 20;
        
        PageHelper.startPage(currentPage, pageSize);
        
        // 前台只展示：已上架(isMarketable='1') + 审核通过(auditStatus='2') + 未删除(isDelete='0')
        String goodsName = null;
        Long category1Id = null;
        Double minPrice = null;
        Double maxPrice = null;
        
        if (queryParams.getParams() != null) {
            goodsName = queryParams.getParams().getGoodsName();
            category1Id = queryParams.getParams().getCategory1Id();
            category1Id = queryParams.getParams().getCategory1Id();
            minPrice = queryParams.getParams().getPrice() != null ? queryParams.getParams().getPrice().doubleValue() : null;
        }
        
        // 从 queryParams 中获取额外的筛选参数
        if (queryParams.getParams() != null) {
            // 这里可以使用自定义的查询参数扩展
            // 实际项目中可以扩展 TbGoods 实体或使用 Map 存储额外参数
        }
        
        // 查询已上架且审核通过的商品（不传 sellerId，查询所有商家）
        List<TbGoodsVo> list = goodsMapper.selectPublishedGoods(goodsName, category1Id, minPrice, maxPrice);
        
        long total = ((Page<?>) list).getTotal();
        
        return new PageList<>(total, list);
    }

    @Override
    public Map<String, Object> getDetailById(Long id) {
        TbGoods goods = goodsMapper.selectByPrimaryKey(id);

        // 校验商品是否存在、已上架、审核通过、未删除
        if (goods == null) {
            throw new BusinessException("商品不存在！");
        }
        if (!"1".equals(goods.getIsMarketable())) {
            throw new BusinessException("商品已下架，无法查看！");
        }
        if (!"2".equals(goods.getAuditStatus())) {
            throw new BusinessException("商品未通过审核，无法查看！");
        }
        if ("1".equals(goods.getIsDelete())) {
            throw new BusinessException("商品已删除！");
        }

        // 查询商品详情扩展表
        TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);

        // 查询SKU列表
        TbItemExample itemExample = new TbItemExample();
        itemExample.createCriteria().andGoodsIdEqualTo(id);
        itemExample.setOrderByClause("is_default DESC, id ASC");
        List<TbItem> itemList = itemMapper.selectByExample(itemExample);

        // 组装返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("goods", goods);
        result.put("goodsDesc", goodsDesc);
        result.put("itemList", itemList);
        result.put("staticUrl", goods.getStaticUrl());

        return result;
    }

    @Override
    public Map<String, Object> getAdminDetailById(Long id) {
        TbGoods goods = goodsMapper.selectByPrimaryKey(id);

        if (goods == null) {
            throw new BusinessException("商品不存在！");
        }
        if ("1".equals(goods.getIsDelete())) {
            throw new BusinessException("商品已删除！");
        }

        // 查询商品详情扩展表
        TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);

        // 查询SKU列表
        TbItemExample itemExample = new TbItemExample();
        itemExample.createCriteria().andGoodsIdEqualTo(id);
        itemExample.setOrderByClause("is_default DESC, id ASC");
        List<TbItem> itemList = itemMapper.selectByExample(itemExample);

        // 组装返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("goods", goods);
        result.put("goodsDesc", goodsDesc);
        result.put("itemList", itemList);

        return result;
    }

    // ========== 商家端规格模板接口实现 ==========

    @Override
    public Map<String, Object> getTemplateByCategory(Long categoryId) {
        // 1. 根据分类ID获取分类信息，找到 typeId（模板ID）
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(categoryId);
        if (itemCat == null) {
            throw new BusinessException("分类不存在！");
        }
        
        Long typeId = itemCat.getTypeId();
        if (typeId == null) {
            // 如果该分类没有模板，返回空数据
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("template", null);
            emptyResult.put("brands", new ArrayList<>());
            emptyResult.put("specs", new ArrayList<>());
            emptyResult.put("customAttributes", new ArrayList<>());
            return emptyResult;
        }
        
        // 2. 根据模板ID获取模板详情
        TbTypeTemplate template = typeTemplateMapper.selectByPrimaryKey(typeId);
        if (template == null) {
            // 模板被删除或不存在，返回空数据而不是抛异常
            log.warn("模板不存在: typeId={}", typeId);
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("template", null);
            emptyResult.put("brands", new ArrayList<>());
            emptyResult.put("specs", new ArrayList<>());
            emptyResult.put("customAttributes", new ArrayList<>());
            return emptyResult;
        }
        
        // 3. 解析模板中的 JSON 字段
        Map<String, Object> result = new HashMap<>();
        result.put("template", template);
        
        // 解析品牌列表
        List<Map<String, Object>> brands = new ArrayList<>();
        if (template.getBrandIds() != null && !template.getBrandIds().isEmpty()) {
            try {
                JSONArray brandArray = JSON.parseArray(template.getBrandIds());
                for (int i = 0; i < brandArray.size(); i++) {
                    JSONObject brandObj = brandArray.getJSONObject(i);
                    Map<String, Object> brand = new HashMap<>();
                    brand.put("id", brandObj.getLong("id"));
                    brand.put("name", brandObj.getString("text"));
                    brands.add(brand);
                }
            } catch (Exception e) {
                log.error("解析品牌JSON失败", e);
            }
        }
        result.put("brands", brands);
        
        // 解析规格ID列表
        List<Long> specIds = new ArrayList<>();
        if (template.getSpecIds() != null && !template.getSpecIds().isEmpty()) {
            try {
                JSONArray specArray = JSON.parseArray(template.getSpecIds());
                for (int i = 0; i < specArray.size(); i++) {
                    JSONObject specObj = specArray.getJSONObject(i);
                    specIds.add(specObj.getLong("id"));
                }
            } catch (Exception e) {
                log.error("解析规格ID JSON失败", e);
            }
        }
        
        // 获取规格详情
        List<Map<String, Object>> specs = getSpecsDetail(specIds);
        result.put("specs", specs);
        
        // 解析扩展属性
        List<Map<String, Object>> customAttributes = new ArrayList<>();
        if (template.getCustomAttributeItems() != null && !template.getCustomAttributeItems().isEmpty()) {
            try {
                JSONArray attrArray = JSON.parseArray(template.getCustomAttributeItems());
                for (int i = 0; i < attrArray.size(); i++) {
                    JSONObject attrObj = attrArray.getJSONObject(i);
                    Map<String, Object> attr = new HashMap<>();
                    attr.put("text", attrObj.getString("text"));
                    attr.put("value", attrObj.getString("value"));
                    customAttributes.add(attr);
                }
            } catch (Exception e) {
                log.error("解析扩展属性JSON失败", e);
            }
        }
        result.put("customAttributes", customAttributes);
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getSpecsDetail(List<Long> specIds) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        if (specIds == null || specIds.isEmpty()) {
            return result;
        }
        
        for (Long specId : specIds) {
            // 获取规格名称
            TbSpecification spec = specificationMapper.selectByPrimaryKey(specId);
            if (spec == null) continue;
            
            Map<String, Object> specMap = new HashMap<>();
            specMap.put("id", specId);
            specMap.put("specName", spec.getSpecName());
            
            // 获取该规格下的所有选项
            TbSpecificationOptionExample example = new TbSpecificationOptionExample();
            example.createCriteria().andSpecIdEqualTo(specId);
            example.setOrderByClause("orders asc");
            List<TbSpecificationOption> options = specificationOptionMapper.selectByExample(example);
            
            List<Map<String, Object>> optionList = new ArrayList<>();
            for (TbSpecificationOption option : options) {
                Map<String, Object> optionMap = new HashMap<>();
                optionMap.put("id", option.getId());
                optionMap.put("optionName", option.getOptionName());
                optionMap.put("orders", option.getOrders());
                optionList.add(optionMap);
            }
            specMap.put("options", optionList);
            
            result.add(specMap);
        }
        
        return result;
    }
}