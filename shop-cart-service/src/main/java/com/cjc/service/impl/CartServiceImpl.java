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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 购物车服务实现（Redis版本）
 * 使用Redis Hash存储购物车数据
 * Key: cart:user:{userId}
 * Field: {goodsId}_{itemId}
 * Value: JSON字符串（CartItem）
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
     * 构建CartVo（查询商品信息）
     */
    private CartVo buildCartVo(CartItem cartItem) {
        CartVo vo = new CartVo();
        // 使用goodsId_itemId作为唯一标识（前端selectedIds使用）
        vo.setId(cartItem.getGoodsId() + "_"  + cartItem.getItemId());
        vo.setGoodsId(cartItem.getGoodsId());
        vo.setItemId(cartItem.getItemId());
        vo.setNum(cartItem.getNum());
        vo.setChecked(true); // 默认选中

        // 查询商品信息
        TbGoods goods = goodsMapper.selectGoodsById(cartItem.getGoodsId());
        if (goods != null) {
            vo.setGoodsName(goods.getGoodsName());
            vo.setImage(goods.getSmallPic());
            vo.setSellerId(goods.getSellerId());

            // 查询SKU信息
            TbItem item = null;
            if (cartItem.getItemId() != null) {
                item = itemMapper.selectItemById(cartItem.getItemId());
            }

            if (item == null && goods.getDefaultItemId() != null) {
                item = itemMapper.selectItemById(goods.getDefaultItemId());
            }

            if (item == null) {
                item = itemMapper.selectDefaultItemByGoodsId(cartItem.getGoodsId());
            }

            if (item == null) {
                item = itemMapper.selectFirstItemByGoodsId(cartItem.getGoodsId());
            }

            if (item != null) {
                vo.setPrice(item.getPrice());
                vo.setSpec(item.getSpec());
                vo.setStockCount(item.getStockCount());
            } else {
                // 使用商品价格作为兜底
                vo.setPrice(goods.getPrice());
                vo.setStockCount(0);
            }
        }

        // 计算小计
        if (vo.getPrice() != null && cartItem.getNum() != null) {
            vo.setTotalPrice(vo.getPrice().multiply(new BigDecimal(cartItem.getNum())));
        }

        return vo;
    }

    @Override
    public List<CartVo> listWithGoods(Long userId) {
        String key = getCartKey(userId);

        // 从Redis获取全部购物车商品
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        if (entries == null || entries.isEmpty()) {
            return new ArrayList<>();
        }

        List<CartVo> result = new ArrayList<>();

        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            try {
                String json = (String) entry.getValue();
                CartItem cartItem = JSON.parseObject(json, CartItem.class);

                // 构建CartVo，查询商品信息
                CartVo vo = buildCartVo(cartItem);
                result.add(vo);
            } catch (Exception e) {
                log.warn("解析购物车数据失败: {}", entry.getValue(), e);
            }
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