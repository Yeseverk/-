package com.cjc.service;

import com.cjc.dto.OrderCreateDto;
import com.cjc.pojo.TbOrder;
import com.cjc.vo.OrderPreviewVo;
import com.cjc.vo.OrderVo;

import java.util.List;

/**
 * 订单服务接口（Redis购物车版本）
 */
public interface OrderService {

    /**
     * 订单预览（根据商品列表获取待结算商品）
     * @param items 商品列表（格式：goodsId_itemId）
     * @param userId 用户ID
     */
    OrderPreviewVo preview(List<String> items, String userId);

    /**
     * 创建订单
     * @return 订单ID
     */
    Long create(OrderCreateDto dto, String userId);

    /**
     * 获取用户订单列表
     */
    List<OrderVo> list(String userId, String status);

    /**
     * 获取订单详情
     */
    OrderVo getById(Long orderId, String userId);

    /**
     * 取消订单（未付款状态）
     */
    void cancel(Long orderId, String userId);

    /**
     * 确认收货
     */
    void confirmReceive(Long orderId, String userId);

    /**
     * 删除订单（已完成或已关闭状态）
     */
    void delete(Long orderId, String userId);

    /**
     * 支付成功回调（更新订单状态）
     */
    void paySuccess(Long orderId, String transactionId);

    /**
     * 发货（商家端）
     */
    void deliver(Long orderId, String sellerId);

    /**
     * 获取商家订单列表（商家端）
     */
    List<OrderVo> sellerList(String sellerId, String status);
}