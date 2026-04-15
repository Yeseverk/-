package com.cjc.service.impl;

import com.alibaba.fastjson2.JSON;
import com.cjc.constant.OrderStatus;
import com.cjc.dto.OrderCreateDto;
import com.cjc.exception.BusinessException;
import com.cjc.mapper.*;
import com.cjc.pojo.*;
import com.cjc.service.AddressService;
import com.cjc.service.OrderService;
import com.cjc.util.IdWorker;
import com.cjc.vo.AddressVo;
import com.cjc.vo.OrderPreviewVo;
import com.cjc.vo.OrderVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 订单服务实现（Redis购物车版本）
 * 从Redis读取购物车数据，下单后从Redis删除
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
     * 购物车商品项（内部类）
     */
    private static class CartItemData {
        Long goodsId;
        Long itemId;
        Integer num;

        CartItemData(Long goodsId, Long itemId, Integer num) {
            this.goodsId = goodsId;
            this.itemId = itemId;
            this.num = num;
        }
    }

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

        OrderPreviewVo previewVo = new OrderPreviewVo();
        List<OrderPreviewVo.OrderItemPreviewVo> cartList = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (String itemStr : items) {
            Long[] parsed = parseItem(itemStr);
            Long goodsId = parsed[0];
            Long itemId = parsed[1];
            String field = goodsId + "_" + (itemId != null ? itemId : 0);

            // 从Redis获取购物车商品数据
            String json = (String) cartData.get(field);
            if (json == null) {
                throw new BusinessException("购物车商品不存在: " + itemStr);
            }

            // 解析购物车数据
            Map<String, Object> cartItem = JSON.parseObject(json, Map.class);
            Integer num = (Integer) cartItem.get("num");
            Long finalItemId = cartItem.get("itemId") != null ?
                Long.parseLong(cartItem.get("itemId").toString()) : itemId;

            // 查询商品信息
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            if (goods == null) {
                throw new BusinessException("商品不存在");
            }

            // 查询SKU信息
            TbItem item = null;
            if (finalItemId != null) {
                item = itemMapper.selectByPrimaryKey(finalItemId);
            }
            if (item == null && goods.getDefaultItemId() != null) {
                item = itemMapper.selectByPrimaryKey(goods.getDefaultItemId());
            }
            if (item == null) {
                item = itemMapper.selectDefaultItemByGoodsId(goodsId);
            }
            if (item == null) {
                item = itemMapper.selectFirstItemByGoodsId(goodsId);
            }
            if (item == null) {
                throw new BusinessException("商品【" + goods.getGoodsName() + "】暂无可用SKU");
            }

            // 校验库存
            Integer stockCount = item.getStockCount();
            if (stockCount == null || stockCount < (num != null ? num : 1)) {
                throw new BusinessException("商品【" + goods.getGoodsName() + "】库存不足");
            }

            // 构建预览项
            OrderPreviewVo.OrderItemPreviewVo itemVo = new OrderPreviewVo.OrderItemPreviewVo();
            itemVo.setCartId(0L); // Redis购物车无cartId，设为0
            itemVo.setGoodsId(goods.getId());
            itemVo.setItemId(item.getId());
            itemVo.setGoodsName(goods.getGoodsName());
            itemVo.setImage(goods.getSmallPic());
            itemVo.setPrice(item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);
            itemVo.setNum(num != null ? num : 1);
            itemVo.setSpec(item.getSpec());
            itemVo.setSellerId(goods.getSellerId());
            itemVo.setStockCount(stockCount);

            // 计算小计
            BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
            int quantity = num != null ? num : 1;
            BigDecimal itemTotal = price.multiply(new BigDecimal(quantity));
            itemVo.setTotalPrice(itemTotal);
            totalPrice = totalPrice.add(itemTotal);

            cartList.add(itemVo);
        }

        previewVo.setCartList(cartList);
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
    @Transactional
    public Long create(OrderCreateDto dto, String userId) {
        log.info("用户[{}]开始创建订单, 商品列表: {}", userId, dto.getItems());

        // 1. 订单预览（校验商品和库存）
        OrderPreviewVo previewVo = preview(dto.getItems(), userId);

        // 2. 获取收货地址
        AddressVo address = addressService.getById(dto.getAddressId(), userId);
        if (address == null) {
            throw new BusinessException("请选择收货地址");
        }

        // 3. 扣减库存（乐观锁）
        for (OrderPreviewVo.OrderItemPreviewVo itemVo : previewVo.getCartList()) {
            int rows = itemMapper.deductStock(itemVo.getItemId(), itemVo.getNum());
            if (rows == 0) {
                throw new BusinessException("商品【" + itemVo.getGoodsName() + "】库存不足，下单失败");
            }
        }

        // 4. 生成订单号（雪花算法）
        Long orderId = idWorker.nextId();

        // 5. 创建订单主表
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

        // 6. 创建订单商品表
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

        // 7. 从Redis删除已结算的购物车商品
        String cartKey = getCartKey(userId);
        List<Object> fields = new ArrayList<>();
        for (String itemStr : dto.getItems()) {
            Long[] parsed = parseItem(itemStr);
            String field = parsed[0] + "_" + (parsed[1] != null ? parsed[1] : 0);
            fields.add(field);
        }
        redisTemplate.opsForHash().delete(cartKey, fields.toArray());

        log.info("订单创建成功, 订单号: {}, 用户: {}, 商家: {}", orderId, userId, sellerId);
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

        TbOrderItemExample itemExample = new TbOrderItemExample();
        itemExample.createCriteria().andOrderIdEqualTo(orderId);
        List<TbOrderItem> orderItems = orderItemMapper.selectByExample(itemExample);

        for (TbOrderItem orderItem : orderItems) {
            itemMapper.updateSales(orderItem.getItemId(), orderItem.getNum());
        }
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

        for (TbOrderItem orderItem : orderItems) {
            itemMapper.releaseStock(orderItem.getItemId(), orderItem.getNum());
        }
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