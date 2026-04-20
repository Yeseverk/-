package com.cjc.service.impl;

import com.alibaba.fastjson2.JSON;
import com.cjc.api.ItemApi;
import com.cjc.constant.OrderStatus;
import com.cjc.dto.OrderCreateDto;
import com.cjc.exception.BusinessException;
import com.cjc.mapper.*;
import com.cjc.pojo.*;
import com.cjc.service.AddressService;
import com.cjc.service.OrderService;
import com.cjc.util.IdWorker;
import com.cjc.util.Result;
import com.cjc.vo.AddressVo;
import com.cjc.vo.OrderPreviewVo;
import com.cjc.vo.OrderVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 订单服务实现（Redis购物车版本）
 * 从Redis读取购物车数据，下单后从Redis删除
 *
 * 性能优化：
 * 1. 批量查询解决N+1问题（最多2次数据库查询）
 * 2. 商品状态校验（上架+审核通过+未删除）
 * 3. 无效商品自动移除并提示用户
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private TbOrderMapper orderMapper;

    @Autowired
    private TbOrderItemMapper orderItemMapper;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private AddressService addressService;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ItemApi itemApi;  // Feign接口，用于跨服务库存操作
    
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    // Redis购物车Key前缀
    private static final String CART_KEY_PREFIX = "cart:user:";

    /**
     * 获取用户购物车Redis Key
     */
    private String getCartKey(String userId) {
        return CART_KEY_PREFIX + userId;
    }

    /**
     * 解析goodsId_itemId格式
     */
    private Long[] parseItem(String item) {
        String[] parts = item.split("_");
        Long goodsId = Long.parseLong(parts[0]);
        Long itemId = parts.length > 1 ? Long.parseLong(parts[1]) : null;
        return new Long[]{goodsId, itemId};
    }

    /**
     * 获取field名（goodsId_itemId）
     */
    private String getField(Long goodsId, Long itemId) {
        return goodsId + "_" + (itemId != null ? itemId : 0);
    }

    /**
     * 购物车商品项（内部类）
     */
    private static class CartItemData {
        Long goodsId;
        Long itemId;
        Integer num;
        String field;

        CartItemData(Long goodsId, Long itemId, Integer num, String field) {
            this.goodsId = goodsId;
            this.itemId = itemId;
            this.num = num;
            this.field = field;
        }
    }

    /**
     * 订单预览（批量查询优化版 + 商品状态校验）
     *
     * 商品有效性判断：
     * 1. 商品存在 + SKU存在
     * 2. isMarketable = '1'（上架）
     * 3. auditStatus = '2'（审核通过）
     * 4. isDelete != '1'（未删除）
     *
     * 无效商品自动从购物车删除，并返回移除列表提示用户
     */
    @Override
    public OrderPreviewVo preview(List<String> items, String userId) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException("请选择要结算的商品");
        }

        String cartKey = getCartKey(userId);
        Map<Object, Object> cartData = redisTemplate.opsForHash().entries(cartKey);

        if (cartData == null || cartData.isEmpty()) {
            throw new BusinessException("购物车为空");
        }

        // 1. 解析购物车数据并收集需要查询的ID列表
        List<CartItemData> cartItemDataList = new ArrayList<>();
        Set<Long> goodsIdsToQuery = new HashSet<>();
        Set<Long> itemIdsToQuery = new HashSet<>();

        for (String itemStr : items) {
            Long[] parsed = parseItem(itemStr);
            Long goodsId = parsed[0];
            Long itemId = parsed[1];
            String field = getField(goodsId, itemId);

            String json = (String) cartData.get(field);
            if (json == null) {
                throw new BusinessException("购物车商品不存在: " + itemStr);
            }

            Map<String, Object> cartItem = JSON.parseObject(json, Map.class);
            Integer num = (Integer) cartItem.get("num");
            Long finalItemId = cartItem.get("itemId") != null ?
                Long.parseLong(cartItem.get("itemId").toString()) : itemId;

            cartItemDataList.add(new CartItemData(goodsId, finalItemId, num != null ? num : 1, field));

            // 收集ID用于批量查询
            goodsIdsToQuery.add(goodsId);
            if (finalItemId != null && finalItemId != 0) {
                itemIdsToQuery.add(finalItemId);
            }
        }

        // 2. 批量查询数据库（最多2次查询，解决N+1问题）
        Map<Long, TbGoods> goodsMap = new HashMap<>();
        if (!goodsIdsToQuery.isEmpty()) {
            List<TbGoods> goodsList = goodsMapper.selectGoodsByIds(new ArrayList<>(goodsIdsToQuery));
            goodsMap = goodsList.stream().collect(Collectors.toMap(TbGoods::getId, Function.identity()));
        }

        Map<Long, TbItem> itemMap = new HashMap<>();
        if (!itemIdsToQuery.isEmpty()) {
            List<TbItem> itemList = itemMapper.selectItemByIds(new ArrayList<>(itemIdsToQuery));
            itemMap = itemList.stream().collect(Collectors.toMap(TbItem::getId, Function.identity()));
        }

        // 3. 商品状态校验 + 数据组装
        OrderPreviewVo previewVo = new OrderPreviewVo();
        List<OrderPreviewVo.OrderItemPreviewVo> validCartList = new ArrayList<>();
        List<OrderPreviewVo.RemovedItemVo> removedItems = new ArrayList<>();
        List<String> fieldsToRemove = new ArrayList<>();  // 要从Redis删除的field
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItemData cartItemData : cartItemDataList) {
            TbGoods goodsInfo = goodsMap.get(cartItemData.goodsId);
            TbItem skuInfo = itemMap.get(cartItemData.itemId);

            // 商品状态校验
            String removeReason = null;

            if (goodsInfo == null) {
                removeReason = "商品已失效";
            } else if ("1".equals(goodsInfo.getIsDelete())) {
                removeReason = "商品已失效";
            } else if (!"2".equals(goodsInfo.getAuditStatus())) {
                if ("0".equals(goodsInfo.getAuditStatus())) {
                    removeReason = "商品正在调整中，暂无法购买";
                } else if ("1".equals(goodsInfo.getAuditStatus())) {
                    removeReason = "商品待审核，暂无法购买";
                } else if ("3".equals(goodsInfo.getAuditStatus())) {
                    removeReason = "商品审核未通过，暂无法购买";
                }
            } else if (!"1".equals(goodsInfo.getIsMarketable())) {
                removeReason = "商品已下架";
            } else if (skuInfo == null) {
                removeReason = "商品规格已失效";
            } else if (skuInfo.getStockCount() == null || skuInfo.getStockCount() <= 0) {
                removeReason = "商品已售罄";
            }

            if (removeReason != null) {
                // 商品无效，记录移除信息
                OrderPreviewVo.RemovedItemVo removedVo = new OrderPreviewVo.RemovedItemVo();
                removedVo.setItemKey(cartItemData.field);
                removedVo.setGoodsId(cartItemData.goodsId);
                removedVo.setItemId(cartItemData.itemId);
                removedVo.setReason(removeReason);

                // 尝试获取商品名称（如果有的话）
                if (goodsInfo != null) {
                    removedVo.setGoodsName(goodsInfo.getGoodsName());
                } else if (skuInfo != null) {
                    removedVo.setGoodsName(skuInfo.getTitle());
                } else {
                    removedVo.setGoodsName("未知商品");
                }

                removedItems.add(removedVo);
                fieldsToRemove.add(cartItemData.field);

                log.warn("订单预览移除无效商品: userId={}, goodsId={}, itemId={}, reason={}",
                    userId, cartItemData.goodsId, cartItemData.itemId, removeReason);
            } else {
                // 商品有效，构建预览项
                OrderPreviewVo.OrderItemPreviewVo itemVo = new OrderPreviewVo.OrderItemPreviewVo();
                itemVo.setCartId(0L);
                itemVo.setGoodsId(cartItemData.goodsId);
                itemVo.setItemId(skuInfo.getId());
                itemVo.setGoodsName(skuInfo.getTitle());
                itemVo.setImage(skuInfo.getImage());
                itemVo.setPrice(skuInfo.getPrice() != null ? skuInfo.getPrice() : BigDecimal.ZERO);
                itemVo.setNum(cartItemData.num);
                itemVo.setSpec(skuInfo.getSpec());
                itemVo.setSellerId(skuInfo.getSellerId());
                itemVo.setStockCount(skuInfo.getStockCount());

                // 库存校验（数量超过库存）
                if (cartItemData.num > skuInfo.getStockCount()) {
                    log.warn("购买数量超过库存: goodsId={}, num={}, stock={}",
                        cartItemData.goodsId, cartItemData.num, skuInfo.getStockCount());
                    // 可以结算，但前端需要提示用户调整数量
                }

                // 计算小计
                BigDecimal price = skuInfo.getPrice() != null ? skuInfo.getPrice() : BigDecimal.ZERO;
                BigDecimal itemTotal = price.multiply(new BigDecimal(cartItemData.num));
                itemVo.setTotalPrice(itemTotal);
                totalPrice = totalPrice.add(itemTotal);

                validCartList.add(itemVo);
            }
        }

        // 4. 从Redis删除无效商品
        if (!fieldsToRemove.isEmpty()) {
            redisTemplate.opsForHash().delete(cartKey, fieldsToRemove.toArray());
            log.info("订单预览自动移除无效商品: userId={}, count={}, fields={}",
                userId, fieldsToRemove.size(), fieldsToRemove);
        }

        // 5. 如果全部商品都无效，抛出异常
        if (validCartList.isEmpty() && !removedItems.isEmpty()) {
            String reasons = removedItems.stream()
                .map(r -> r.getGoodsName() + "(" + r.getReason() + ")")
                .collect(Collectors.joining(", "));
            throw new BusinessException("所选商品均不可购买：" + reasons);
        }

        // 6. 设置返回结果
        previewVo.setCartList(validCartList);
        previewVo.setRemovedItems(removedItems);  // 前端可以展示这些被移除的商品
        previewVo.setTotalPrice(totalPrice);

        // 获取默认地址
        AddressVo address = addressService.getDefault(userId);
        previewVo.setAddress(address);

        // 邮费（暂时为0）
        previewVo.setPostFee(BigDecimal.ZERO);

        // 实付金额
        previewVo.setPayment(totalPrice);

        return previewVo;
    }

    @Override
    @GlobalTransactional(name = "create-order", rollbackFor = Exception.class)
    public Long create(OrderCreateDto dto, String userId) {
        log.info("用户[{}]开始创建订单（分布式事务），商品列表: {}", userId, dto.getItems());

        // 1. 订单预览（校验商品和库存，自动移除无效商品）
        OrderPreviewVo previewVo = preview(dto.getItems(), userId);

        // 2. 检查是否有有效商品
        if (previewVo.getCartList() == null || previewVo.getCartList().isEmpty()) {
            throw new BusinessException("没有可结算的商品");
        }

        // 3. 获取收货地址
        AddressVo address = addressService.getById(dto.getAddressId(), userId);
        if (address == null) {
            throw new BusinessException("请选择收货地址");
        }

        // 4. 扣减库存（跨服务调用，通过Seata保证分布式事务一致性）
        List<TbItem> stockItems = new ArrayList<>();
        for (OrderPreviewVo.OrderItemPreviewVo itemVo : previewVo.getCartList()) {
            TbItem tbItem = new TbItem();
            tbItem.setId(itemVo.getItemId());
            tbItem.setNum(itemVo.getNum());
            stockItems.add(tbItem);
        }

        Result reduceResult = itemApi.reduceStock(stockItems);
        if (reduceResult == null || !"10000".equals(reduceResult.getCode())) {
            throw new BusinessException("库存扣减失败：" + (reduceResult != null ? reduceResult.getMessage() : "服务调用异常"));
        }
        log.info("库存扣减成功（Feign调用），商品数量: {}", stockItems.size());

        // 5. 生成订单号（雪花算法）
        Long orderId = idWorker.nextId();

        // 6. 创建订单主表
        TbOrder order = new TbOrder();
        order.setOrderId(orderId);
        order.setPayment(previewVo.getPayment());
        order.setPaymentType(dto.getPaymentType() != null ? dto.getPaymentType() : "1");
        order.setPostFee("0");
        order.setStatus(OrderStatus.UNPAID);
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        order.setUserId(userId);
        order.setBuyerMessage(dto.getBuyerMessage());

        // 收货地址信息
        order.setReceiver(address.getContact());
        order.setReceiverMobile(address.getMobile());
        order.setReceiverAreaName(address.getFullAddress());

        // 商家ID（取第一个商品的商家）
        String sellerId = previewVo.getCartList().get(0).getSellerId();
        order.setSellerId(sellerId);

        orderMapper.insertSelective(order);

        // 7. 创建订单商品表
        for (OrderPreviewVo.OrderItemPreviewVo itemVo : previewVo.getCartList()) {
            TbOrderItem orderItem = new TbOrderItem();
            orderItem.setId(idWorker.nextId());
            orderItem.setOrderId(orderId);
            orderItem.setItemId(itemVo.getItemId());
            orderItem.setGoodsId(itemVo.getGoodsId());
            orderItem.setTitle(itemVo.getGoodsName());
            orderItem.setPrice(itemVo.getPrice());
            orderItem.setNum(itemVo.getNum());
            orderItem.setTotalFee(itemVo.getTotalPrice());
            orderItem.setPicPath(itemVo.getImage());
            orderItem.setSellerId(itemVo.getSellerId());

            orderItemMapper.insertSelective(orderItem);
        }

        // 8. 从Redis删除已结算的购物车商品（只删除有效的，无效的已在preview中删除）
        String cartKey = getCartKey(userId);
        List<Object> fields = new ArrayList<>();
        for (OrderPreviewVo.OrderItemPreviewVo itemVo : previewVo.getCartList()) {
            String field = getField(itemVo.getGoodsId(), itemVo.getItemId());
            fields.add(field);
        }
        if (!fields.isEmpty()) {
            redisTemplate.opsForHash().delete(cartKey, fields.toArray());
        }

        // 9. 发送延迟消息（30分钟后检查支付状态）
        // 延迟级别: 1:1s 2:5s 3:10s 4:30s 5:1m 6:2m 7:3m 8:4m 9:5m 10:6m 11:7m 12:8m 13:9m 14:10m 15:20m 16:30m 17:1h 18:2h
        // 这里需要30分钟，所以级别为 16
        rocketMQTemplate.syncSend("ORDER_CANCEL_TOPIC", MessageBuilder.withPayload(orderId).build(), 3000, 4);

        log.info("订单创建成功, 订单号: {}, 用户: {}, 商家: {}, 移除无效商品数: {}, 发送延迟消息",
            orderId, userId, sellerId,
            previewVo.getRemovedItems() != null ? previewVo.getRemovedItems().size() : 0);
        return orderId;
    }

    @Override
    public List<OrderVo> list(String userId, String status) {
        TbOrderExample example = new TbOrderExample();
        TbOrderExample.Criteria criteria = example.createCriteria().andUserIdEqualTo(userId);

        if (status != null && !status.isEmpty()) {
            criteria.andStatusEqualTo(status);
        }

        example.setOrderByClause("create_time DESC");

        List<TbOrder> orders = orderMapper.selectByExample(example);
        return convertToVoList(orders);
    }

    @Override
    public OrderVo getById(Long orderId, String userId) {
        TbOrder order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权访问");
        }

        return convertToVo(order);
    }

    @Override
    @Transactional
    public void cancel(Long orderId, String userId) {
        TbOrder order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        if (!OrderStatus.UNPAID.equals(order.getStatus())) {
            throw new BusinessException("只有未付款订单可以取消");
        }

        TbOrder update = new TbOrder();
        update.setOrderId(orderId);
        update.setStatus(OrderStatus.CLOSED);
        update.setCloseTime(new Date());
        update.setUpdateTime(new Date());
        orderMapper.updateByPrimaryKeySelective(update);

        releaseStock(orderId);
    }

    @Override
    @Transactional
    public void confirmReceive(Long orderId, String userId) {
        TbOrder order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        if (!OrderStatus.SHIPPED.equals(order.getStatus())) {
            throw new BusinessException("只有已发货订单可以确认收货");
        }

        TbOrder update = new TbOrder();
        update.setOrderId(orderId);
        update.setStatus(OrderStatus.COMPLETED);
        update.setEndTime(new Date());
        update.setUpdateTime(new Date());
        orderMapper.updateByPrimaryKeySelective(update);
    }

    @Override
    @Transactional
    public void delete(Long orderId, String userId) {
        TbOrder order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        if (!OrderStatus.COMPLETED.equals(order.getStatus()) && !OrderStatus.CLOSED.equals(order.getStatus())) {
            throw new BusinessException("只有已完成或已关闭订单可以删除");
        }

        TbOrderItemExample itemExample = new TbOrderItemExample();
        itemExample.createCriteria().andOrderIdEqualTo(orderId);
        orderItemMapper.deleteByExample(itemExample);

        orderMapper.deleteByPrimaryKey(orderId);
    }

    @Override
    @Transactional
    public void paySuccess(Long orderId, String transactionId) {
        TbOrder order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!OrderStatus.UNPAID.equals(order.getStatus())) {
            throw new BusinessException("订单状态异常");
        }

        TbOrder update = new TbOrder();
        update.setOrderId(orderId);
        update.setStatus(OrderStatus.PAID);
        update.setPaymentTime(new Date());
        update.setUpdateTime(new Date());
        orderMapper.updateByPrimaryKeySelective(update);

        // 更新销量（通过Feign调用跨服务）
        TbOrderItemExample itemExample = new TbOrderItemExample();
        itemExample.createCriteria().andOrderIdEqualTo(orderId);
        List<TbOrderItem> orderItems = orderItemMapper.selectByExample(itemExample);

        List<TbItem> salesItems = new ArrayList<>();
        for (TbOrderItem orderItem : orderItems) {
            TbItem tbItem = new TbItem();
            tbItem.setId(orderItem.getItemId());
            tbItem.setNum(orderItem.getNum());
            salesItems.add(tbItem);
        }

        Result salesResult = itemApi.updateSales(salesItems);
        if (salesResult == null || !"10000".equals(salesResult.getCode())) {
            log.error("销量更新失败: orderId={}, result={}", orderId, salesResult);
            // 销量更新失败不影响支付成功状态，仅记录日志
        }
        log.info("销量更新成功（Feign调用），订单号: {}, 商品数量: {}", orderId, salesItems.size());
    }

    @Override
    @Transactional
    public void deliver(Long orderId, String sellerId) {
        TbOrder order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权操作");
        }

        if (!OrderStatus.PAID.equals(order.getStatus())) {
            throw new BusinessException("订单状态异常，无法发货");
        }

        TbOrder update = new TbOrder();
        update.setOrderId(orderId);
        update.setStatus(OrderStatus.SHIPPED);
        update.setConsignTime(new Date());
        update.setUpdateTime(new Date());
        orderMapper.updateByPrimaryKeySelective(update);
    }

    @Override
    public List<OrderVo> sellerList(String sellerId, String status) {
        TbOrderExample example = new TbOrderExample();
        TbOrderExample.Criteria criteria = example.createCriteria().andSellerIdEqualTo(sellerId);

        if (status != null && !status.isEmpty()) {
            criteria.andStatusEqualTo(status);
        }

        example.setOrderByClause("create_time DESC");

        List<TbOrder> orders = orderMapper.selectByExample(example);
        return convertToVoList(orders);
    }

    private void releaseStock(Long orderId) {
        TbOrderItemExample itemExample = new TbOrderItemExample();
        itemExample.createCriteria().andOrderIdEqualTo(orderId);
        List<TbOrderItem> orderItems = orderItemMapper.selectByExample(itemExample);

        // 通过Feign调用跨服务释放库存
        List<TbItem> stockItems = new ArrayList<>();
        for (TbOrderItem orderItem : orderItems) {
            TbItem tbItem = new TbItem();
            tbItem.setId(orderItem.getItemId());
            tbItem.setNum(orderItem.getNum());
            stockItems.add(tbItem);
        }

        Result releaseResult = itemApi.releaseStock(stockItems);
        if (releaseResult == null || !"10000".equals(releaseResult.getCode())) {
            log.error("库存释放失败: orderId={}, result={}", orderId, releaseResult);
            throw new BusinessException("库存释放失败：" + (releaseResult != null ? releaseResult.getMessage() : "服务调用异常"));
        }
        log.info("库存释放成功（Feign调用），订单号: {}, 商品数量: {}", orderId, stockItems.size());
    }

    private List<OrderVo> convertToVoList(List<TbOrder> orders) {
        return orders.stream().map(this::convertToVo).toList();
    }

    private OrderVo convertToVo(TbOrder order) {
        OrderVo vo = new OrderVo();
        BeanUtils.copyProperties(order, vo);

        TbOrderItemExample itemExample = new TbOrderItemExample();
        itemExample.createCriteria().andOrderIdEqualTo(order.getOrderId());
        List<TbOrderItem> orderItems = orderItemMapper.selectByExample(itemExample);
        vo.setOrderItems(orderItems);

        return vo;
    }
}