package com.cjc.service;

import com.cjc.vo.PayVo;

/**
 * 支付服务接口（模拟支付）
 */
public interface PayService {

    /**
     * 创建支付（生成支付记录）
     * @return 支付信息（含支付单号、二维码URL等）
     */
    PayVo createPay(Long orderId, String userId);

    /**
     * 查询支付状态
     */
    PayVo queryPayStatus(String outTradeNo);

    /**
     * 模拟支付成功回调
     */
    void mockPaySuccess(String outTradeNo);
}