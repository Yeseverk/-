package com.cjc.service.impl;

import com.alibaba.fastjson2.JSON;
import com.cjc.dto.CartItem;
import com.cjc.exception.BusinessException;
import com.cjc.mapper.TbGoodsMapper;
import com.cjc.mapper.TbItemMapper;
import com.cjc.pojo.TbGoods;
import com.cjc.pojo.TbItem;
import com.cjc.service.CartService;
import com.cjc.vo.CartVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 购物车服务实现（Redis版本）
 * 使用Redis Hash存储购物车数据
 * Key: cart:user:{userId}
 * Field: {goodsId}_{itemId}
 * Value: JSON字符串（CartItem）
 *
 * 性能优化：批量查询解决N+1问题，无论购物车有几件商品，最多只查2次数据库
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbItemMapper itemMapper;

    // Redis Key前缀
    private static final String CART_KEY_PREFIX = "cart:user:";

    /**
     * 获取用户购物车Redis Key
     */
    private String getCartKey(Long userId) {
        return CART_KEY_PREFIX + userId;
    }

    /**
     * 获取商品field名（goodsId_itemId）
     */
    private String getField(Long goodsId, Long itemId) {
        return goodsId + "_" + (itemId != null ? itemId : 0);
    }

    /**
     * 查找SKU ID（多重兜底策略）
     */
    private Long findItemId(Long goodsId, Long itemId) {
        // 1. 如果传了itemId，直接用
        if (itemId != null) {
            TbItem item = itemMapper.selectItemById(itemId);
            if (item != null) {
                return itemId;
            }
        }

        // 2. 尝试商品的defaultItemId
        TbGoods goods = goodsMapper.selectGoodsById(goodsId);
        if (goods != null && goods.getDefaultItemId() != null) {
            TbItem defaultItem = itemMapper.selectItemById(goods.getDefaultItemId());
            if (defaultItem != null) {
                return goods.getDefaultItemId();
            }
        }

        // 3. 查询商品的默认SKU记录
        TbItem defaultItem = itemMapper.selectDefaultItemByGoodsId(goodsId);
        if (defaultItem != null) {
            return defaultItem.getId();
        }

        // 4. 查询任意一个SKU作为兜底
        TbItem firstItem = itemMapper.selectFirstItemByGoodsId(goodsId);
        if (firstItem != null) {
            return firstItem.getId();
        }

        return null;
    }

    /**
     * 获取用户购物车列表（批量查询优化版）
     * 解决N+1查询问题：无论购物车有几件商品，最多只查2次数据库
     *
     * 商品有效性判断：
     * 1. 商品存在 + SKU存在
     * 2. isMarketable = '1'（上架）
     * 3. auditStatus = '2'（审核通过）
     * 4. isDelete != '1'（未删除）
     */
    @Override
    public List<CartVo> listWithGoods(Long userId) {
        String key = getCartKey(userId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        if (entries == null || entries.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 解析 Redis 数据并收集需要查询的 ID 列表
        List<CartItem> cartItems = new ArrayList<>();
        Set<Long> itemIdsToQuery = new HashSet<>();
        Set<Long> goodsIdsToQuery = new HashSet<>();

        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            try {
                CartItem cartItem = JSON.parseObject((String) entry.getValue(), CartItem.class);
                if (cartItem != null) {
                    cartItems.add(cartItem);
                    // 收集所有商品ID（用于查询商品状态）
                    if (cartItem.getGoodsId() != null) {
                        goodsIdsToQuery.add(cartItem.getGoodsId());
                    }
                    // 收集有itemId的（用于查询SKU）
                    if (cartItem.getItemId() != null && cartItem.getItemId() != 0) {
                        itemIdsToQuery.add(cartItem.getItemId());
                    }
                }
            } catch (Exception e) {
                log.warn("解析购物车数据失败: {}", entry.getValue(), e);
            }
        }

        if (cartItems.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 批量查询数据库（最多2次查询）
        Map<Long, TbItem> itemMap = new HashMap<>();
        if (!itemIdsToQuery.isEmpty()) {
            List<TbItem> items = itemMapper.selectItemByIds(new ArrayList<>(itemIdsToQuery));
            itemMap = items.stream().collect(Collectors.toMap(TbItem::getId, Function.identity()));
        }

        Map<Long, TbGoods> goodsMap = new HashMap<>();
        if (!goodsIdsToQuery.isEmpty()) {
            List<TbGoods> goodsList = goodsMapper.selectGoodsByIds(new ArrayList<>(goodsIdsToQuery));
            goodsMap = goodsList.stream().collect(Collectors.toMap(TbGoods::getId, Function.identity()));
        }

        // 3. 在内存中完成数据组装 + 状态校验
        List<CartVo> result = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            CartVo vo = new CartVo();
            vo.setId(cartItem.getGoodsId() + "_" + cartItem.getItemId());
            vo.setGoodsId(cartItem.getGoodsId());
            vo.setItemId(cartItem.getItemId());
            vo.setNum(cartItem.getNum());
            vo.setChecked(true);

            TbGoods goodsInfo = goodsMap.get(cartItem.getGoodsId());
            TbItem skuInfo = itemMap.get(cartItem.getItemId());

            // 商品状态校验
            if (goodsInfo == null) {
                // 商品完全不存在
                vo.setGoodsName("商品已失效");
                vo.setPrice(BigDecimal.ZERO);
                vo.setStockCount(0);
                vo.setValid(false);
                vo.setStatusMsg("商品已失效");
            } else if ("1".equals(goodsInfo.getIsDelete())) {
                // 商品已删除
                vo.setGoodsName(goodsInfo.getGoodsName() + "（已失效）");
                vo.setImage(goodsInfo.getSmallPic());
                vo.setPrice(BigDecimal.ZERO);
                vo.setStockCount(0);
                vo.setSellerId(goodsInfo.getSellerId());
                vo.setIsMarketable(goodsInfo.getIsMarketable());
                vo.setAuditStatus(goodsInfo.getAuditStatus());
                vo.setIsDelete(goodsInfo.getIsDelete());
                vo.setValid(false);
                vo.setStatusMsg("商品已失效");
            } else if (!"2".equals(goodsInfo.getAuditStatus())) {
                // 商品未审核通过（未提交/待审核/审核驳回）
                vo.setGoodsName(goodsInfo.getGoodsName());
                vo.setImage(goodsInfo.getSmallPic());
                vo.setPrice(BigDecimal.ZERO);
                vo.setSellerId(goodsInfo.getSellerId());
                vo.setIsMarketable(goodsInfo.getIsMarketable());
                vo.setAuditStatus(goodsInfo.getAuditStatus());
                vo.setIsDelete(goodsInfo.getIsDelete());
                vo.setValid(false);

                // 根据审核状态给出不同提示
                if ("0".equals(goodsInfo.getAuditStatus())) {
                    vo.setStatusMsg("商品正在调整中");
                } else if ("1".equals(goodsInfo.getAuditStatus())) {
                    vo.setStatusMsg("商品待审核");
                } else if ("3".equals(goodsInfo.getAuditStatus())) {
                    vo.setStatusMsg("商品审核未通过");
                }
            } else if (!"1".equals(goodsInfo.getIsMarketable())) {
                // 商品已下架
                vo.setGoodsName(goodsInfo.getGoodsName());
                vo.setImage(goodsInfo.getSmallPic());
                vo.setPrice(BigDecimal.ZERO);
                vo.setSellerId(goodsInfo.getSellerId());
                vo.setIsMarketable(goodsInfo.getIsMarketable());
                vo.setAuditStatus(goodsInfo.getAuditStatus());
                vo.setIsDelete(goodsInfo.getIsDelete());
                vo.setValid(false);
                vo.setStatusMsg("商品已下架");
            } else if (skuInfo == null) {
                // SKU不存在
                vo.setGoodsName(goodsInfo.getGoodsName() + "（规格已失效）");
                vo.setImage(goodsInfo.getSmallPic());
                vo.setPrice(BigDecimal.ZERO);
                vo.setStockCount(0);
                vo.setSellerId(goodsInfo.getSellerId());
                vo.setIsMarketable(goodsInfo.getIsMarketable());
                vo.setAuditStatus(goodsInfo.getAuditStatus());
                vo.setIsDelete(goodsInfo.getIsDelete());
                vo.setValid(false);
                vo.setStatusMsg("商品规格已失效");
            } else {
                // 商品正常可用
                vo.setGoodsName(skuInfo.getTitle());
                vo.setPrice(skuInfo.getPrice());
                vo.setStockCount(skuInfo.getStockCount());
                vo.setImage(skuInfo.getImage());
                vo.setSpec(skuInfo.getSpec());
                vo.setSellerId(skuInfo.getSellerId());
                vo.setIsMarketable(goodsInfo.getIsMarketable());
                vo.setAuditStatus(goodsInfo.getAuditStatus());
                vo.setIsDelete(goodsInfo.getIsDelete());

                // 库存校验
                if (skuInfo.getStockCount() == null || skuInfo.getStockCount() <= 0) {
                    vo.setValid(false);
                    vo.setStatusMsg("商品已售罄");
                } else if (cartItem.getNum() > skuInfo.getStockCount()) {
                    vo.setValid(true);  // 可以结算，但需要调整数量
                    vo.setStatusMsg("库存不足，最大可购买" + skuInfo.getStockCount() + "件");
                } else {
                    vo.setValid(true);
                    vo.setStatusMsg(null);
                }
            }

            // 计算小计（无效商品小计为0）
            if (vo.getValid() && vo.getPrice() != null && cartItem.getNum() != null) {
                vo.setTotalPrice(vo.getPrice().multiply(new BigDecimal(cartItem.getNum())));
            } else {
                vo.setTotalPrice(BigDecimal.ZERO);
            }

            result.add(vo);
        }

        return result;
    }

    @Override
    public void add(Long userId, Long goodsId, Long itemId, Integer num) {
        String key = getCartKey(userId);

        // 查找SKU ID（多重兜底）
        Long finalItemId = findItemId(goodsId, itemId);
        String field = getField(goodsId, finalItemId);

        // 检查Redis中是否已存在
        String existingJson = (String) redisTemplate.opsForHash().get(key, field);

        if (existingJson != null) {
            // 已存在，数量累加
            CartItem existingItem = JSON.parseObject(existingJson, CartItem.class);
            existingItem.setNum(existingItem.getNum() + num);
            existingItem.setUpdateTime(new Date());
            redisTemplate.opsForHash().put(key, field, JSON.toJSONString(existingItem));
            log.info("购物车数量累加: userId={}, goodsId={}, itemId={}, num={}", userId, goodsId, finalItemId, existingItem.getNum());
        } else {
            // 新增购物车项
            CartItem cartItem = new CartItem();
            cartItem.setGoodsId(goodsId);
            cartItem.setItemId(finalItemId);
            cartItem.setNum(num);
            cartItem.setCreateTime(new Date());
            cartItem.setUpdateTime(new Date());
            redisTemplate.opsForHash().put(key, field, JSON.toJSONString(cartItem));
            log.info("购物车新增商品: userId={}, goodsId={}, itemId={}, num={}", userId, goodsId, finalItemId, num);
        }
    }

    @Override
    public void updateNum(Long userId, Long goodsId, Long itemId, Integer num) {
        String key = getCartKey(userId);
        String field = getField(goodsId, itemId);

        if (num <= 0) {
            // 数量为0，删除
            redisTemplate.opsForHash().delete(key, field);
            log.info("购物车删除商品: userId={}, goodsId={}, itemId={}", userId, goodsId, itemId);
        } else {
            // 更新数量
            String json = (String) redisTemplate.opsForHash().get(key, field);
            if (json != null) {
                CartItem cartItem = JSON.parseObject(json, CartItem.class);
                cartItem.setNum(num);
                cartItem.setUpdateTime(new Date());
                redisTemplate.opsForHash().put(key, field, JSON.toJSONString(cartItem));
                log.info("购物车更新数量: userId={}, goodsId={}, itemId={}, num={}", userId, goodsId, itemId, num);
            } else {
                throw new BusinessException("购物车商品不存在");
            }
        }
    }

    @Override
    public void delete(Long userId, Long goodsId, Long itemId) {
        String key = getCartKey(userId);
        String field = getField(goodsId, itemId);
        redisTemplate.opsForHash().delete(key, field);
        log.info("购物车删除商品: userId={}, goodsId={}, itemId={}", userId, goodsId, itemId);
    }

    @Override
    public void batchDelete(Long userId, List<String> fields) {
        if (fields == null || fields.isEmpty()) {
            return;
        }
        String key = getCartKey(userId);
        redisTemplate.opsForHash().delete(key, fields.toArray());
        log.info("购物车批量删除: userId={}, fields={}", userId, fields);
    }

    @Override
    public void batchDeleteByGoodsAndItem(Long userId, List<Long> goodsIds, List<Long> itemIds) {
        if (goodsIds == null || goodsIds.isEmpty()) {
            return;
        }
        String key = getCartKey(userId);
        List<String> fields = new ArrayList<>();
        for (int i = 0; i < goodsIds.size(); i++) {
            Long goodsId = goodsIds.get(i);
            Long itemId = itemIds.get(i);
            fields.add(getField(goodsId, itemId));
        }
        redisTemplate.opsForHash().delete(key, fields.toArray());
        log.info("购物车批量删除(订单结算后): userId={}, fields={}", userId, fields);
    }

    @Override
    public Integer count(Long userId) {
        String key = getCartKey(userId);
        Long size = redisTemplate.opsForHash().size(key);
        return size != null ? size.intValue() : 0;
    }

    @Override
    public void clear(Long userId) {
        String key = getCartKey(userId);
        redisTemplate.delete(key);
        log.info("购物车清空: userId={}", userId);
    }
}