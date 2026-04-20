package com.cjc.mq;

import com.cjc.constant.OrderStatus;
import com.cjc.mapper.TbOrderMapper;
import com.cjc.pojo.TbOrder;
import com.cjc.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = "ORDER_CANCEL_TOPIC", consumerGroup = "order-cancel-group")
public class OrderCancelListener implements RocketMQListener<Long> {

    @Autowired
    private TbOrderMapper orderMapper;

    @Autowired
    private OrderService orderService;

    @Override
    public void onMessage(Long orderId) {
        log.info("接收到延迟取消订单消息: {}", orderId);
        try {
            // 查询订单状态
            TbOrder order = orderMapper.selectByPrimaryKey(orderId);
            if (order == null) {
                log.warn("订单不存在，无需取消: {}", orderId);
                return;
            }

            // 只有未付款的订单才需要取消
            if (OrderStatus.UNPAID.equals(order.getStatus())) {
                log.info("订单超过30分钟未支付，自动取消: {}", orderId);
                // 调用取消订单方法(使用用户的ID进行取消)
                orderService.cancel(orderId, order.getUserId());
            } else {
                log.info("订单已支付或已处理，无需取消: {}, 状态: {}", orderId, order.getStatus());
            }
        } catch (Exception e) {
            log.error("处理延迟取消订单消息异常: {}", orderId, e);
            throw e; // 抛出异常以便RocketMQ重试
        }
    }
}