package com.cjc.controller;

import com.cjc.exception.BusinessException;
import com.cjc.service.PayService;
import com.cjc.util.JwtUtil;
import com.cjc.util.Result;
import com.cjc.vo.PayVo;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "支付管理", description = "支付相关接口（模拟支付）")
@RestController
@RequestMapping("/pay")
public class PayController {

    @Autowired
    private PayService payService;

    /**
     * 创建支付
     */
    @Operation(summary = "创建支付")
    @PostMapping("/create")
    public Result createPay(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String userId = getUserId(request);
        Object orderIdObj = params.get("orderId");
        if (orderIdObj == null) {
            throw new BusinessException("订单ID不能为空");
        }
        Long orderId;
        if (orderIdObj instanceof Long) {
            orderId = (Long) orderIdObj;
        } else if (orderIdObj instanceof Integer) {
            orderId = ((Integer) orderIdObj).longValue();
        } else {
            orderId = Long.parseLong(orderIdObj.toString());
        }
        PayVo payVo = payService.createPay(orderId, userId);
        return Result.success(payVo);
    }

    /**
     * 查询支付状态
     */
    @Operation(summary = "查询支付状态")
    @GetMapping("/status/{outTradeNo}")
    public Result queryPayStatus(@PathVariable String outTradeNo) {
        PayVo payVo = payService.queryPayStatus(outTradeNo);
        return Result.success(payVo);
    }

    /**
     * 模拟支付成功（用于测试）
     */
    @Operation(summary = "模拟支付成功")
    @PostMapping("/mockSuccess/{outTradeNo}")
    public Result mockPaySuccess(@PathVariable String outTradeNo) {
        payService.mockPaySuccess(outTradeNo);
        return Result.success("支付成功");
    }

    /**
     * 从请求中获取用户ID
     */
    private String getUserId(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            throw new BusinessException("请先登录");
        }
        Claims claims = JwtUtil.parseJwt(token);
        return claims.getId();
    }
}