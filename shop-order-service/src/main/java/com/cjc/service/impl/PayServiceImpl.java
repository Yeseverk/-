package com.cjc.service.impl;

import com.cjc.constant.PayStatus;
import com.cjc.exception.BusinessException;
import com.cjc.mapper.TbOrderMapper;
import com.cjc.mapper.TbPayLogMapper;
import com.cjc.pojo.TbOrder;
import com.cjc.pojo.TbPayLog;
import com.cjc.pojo.TbPayLogExample;
import com.cjc.service.OrderService;
import com.cjc.service.PayService;
import com.cjc.util.IdWorker;
import com.cjc.vo.PayVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Service
public class PayServiceImpl implements PayService {

    @Autowired
    private TbPayLogMapper payLogMapper;

    @Autowired
    private TbOrderMapper orderMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private IdWorker idWorker;

    /**
     * 支付过期时间（30分钟）
     */
    private static final long EXPIRE_MINUTES = 30;

    @Override
    @Transactional
    public PayVo createPay(Long orderId, String userId) {
        log.info("创建支付请求, 订单号: {}, 用户: {}", orderId, userId);
        
        // 1. 查询订单
        TbOrder order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            log.warn("创建支付失败: 订单不存在, 订单号: {}", orderId);
            throw new BusinessException("订单不存在");
        }

        // 权限校验
        if (!order.getUserId().equals(userId)) {
            log.warn("创建支付失败: 无权操作, 订单号: {}, 操作用户: {}, 订单用户: {}", 
                orderId, userId, order.getUserId());
            throw new BusinessException("无权操作");
        }

        // 状态校验
        if (!"1".equals(order.getStatus())) {
            log.warn("创建支付失败: 订单状态异常, 订单号: {}, 当前状态: {}", orderId, order.getStatus());
            throw new BusinessException("订单状态异常，无法支付");
        }

        // 2. 检查是否已有未完成的支付记录
        TbPayLog existingLog = getUnpaidPayLog(orderId);
        if (existingLog != null) {
            log.info("返回已存在的支付记录, 支付单号: {}", existingLog.getOutTradeNo());
            return convertToVo(existingLog);
        }

        // 3. 生成支付单号
        String outTradeNo = "PAY" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + String.format("%04d", idWorker.nextId() % 10000);

        // 4. 创建支付记录
        TbPayLog payLog = new TbPayLog();
        payLog.setOutTradeNo(outTradeNo);
        payLog.setCreateTime(new Date());
        BigDecimal payment = order.getPayment() != null ? order.getPayment() : BigDecimal.ZERO;
        payLog.setTotalFee(payment.multiply(new BigDecimal("100")).longValue());
        payLog.setUserId(userId);
        payLog.setTradeState(PayStatus.UNPAID);
        payLog.setOrderList(String.valueOf(orderId));
        payLog.setPayType(order.getPaymentType());

        payLogMapper.insertSelective(payLog);
        log.info("创建支付记录成功, 支付单号: {}, 订单号: {}, 金额: {}分", 
            outTradeNo, orderId, payLog.getTotalFee());

        // 5. 构建支付信息
        PayVo payVo = new PayVo();
        BeanUtils.copyProperties(payLog, payVo);
        payVo.setQrCodeUrl("http://localhost:8086/pay/qr/" + outTradeNo);
        Date expireTime = new Date(System.currentTimeMillis() + EXPIRE_MINUTES * 60 * 1000);
        payVo.setExpireTime(expireTime);

        return payVo;
    }

    @Override
    public PayVo queryPayStatus(String outTradeNo) {
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(outTradeNo);
        if (payLog == null) {
            throw new BusinessException("支付记录不存在");
        }

        return convertToVo(payLog);
    }

    @Override
    @Transactional
    public void mockPaySuccess(String outTradeNo) {
        log.info("模拟支付成功, 支付单号: {}", outTradeNo);
        
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(outTradeNo);
        if (payLog == null) {
            log.error("模拟支付失败: 支付记录不存在, 支付单号: {}", outTradeNo);
            throw new BusinessException("支付记录不存在");
        }

        // 状态校验
        if (!PayStatus.UNPAID.equals(payLog.getTradeState())) {
            log.warn("模拟支付失败: 支付状态异常, 支付单号: {}, 当前状态: {}", 
                outTradeNo, payLog.getTradeState());
            throw new BusinessException("支付状态异常");
        }

        // 1. 更新支付记录状态
        TbPayLog updateLog = new TbPayLog();
        updateLog.setOutTradeNo(outTradeNo);
        updateLog.setTradeState(PayStatus.PAID);
        updateLog.setPayTime(new Date());
        updateLog.setTransactionId("MOCK_" + idWorker.nextId());
        payLogMapper.updateByPrimaryKeySelective(updateLog);
        log.info("支付记录更新成功, 支付单号: {}, 交易流水号: {}", 
            outTradeNo, updateLog.getTransactionId());

        // 2. 更新订单状态
        String orderList = payLog.getOrderList();
        if (orderList != null && !orderList.isEmpty()) {
            Long orderId = Long.parseLong(orderList);
            orderService.paySuccess(orderId, updateLog.getTransactionId());
        }
        
        log.info("模拟支付成功处理完成, 支付单号: {}", outTradeNo);
    }

    /**
     * 获取订单未支付的支付记录
     */
    private TbPayLog getUnpaidPayLog(Long orderId) {
        TbPayLogExample example = new TbPayLogExample();
        example.createCriteria()
            .andOrderListEqualTo(String.valueOf(orderId))
            .andTradeStateEqualTo(PayStatus.UNPAID);

        java.util.List<TbPayLog> logs = payLogMapper.selectByExample(example);
        if (logs != null && !logs.isEmpty()) {
            return logs.get(0);
        }
        return null;
    }

    /**
     * 转换为VO
     */
    private PayVo convertToVo(TbPayLog payLog) {
        PayVo vo = new PayVo();
        BeanUtils.copyProperties(payLog, vo);

        // 设置二维码URL
        vo.setQrCodeUrl("http://localhost:8086/pay/qr/" + payLog.getOutTradeNo());

        // 设置过期时间
        Date createTime = payLog.getCreateTime() != null ? payLog.getCreateTime() : new Date();
        Date expireTime = new Date(createTime.getTime() + EXPIRE_MINUTES * 60 * 1000);
        vo.setExpireTime(expireTime);

        return vo;
    }
}
