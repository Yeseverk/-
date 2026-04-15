package com.cjc.controller;

import com.cjc.dto.OrderCreateDto;
import com.cjc.exception.BusinessException;
import com.cjc.service.OrderService;
import com.cjc.util.JwtUtil;
import com.cjc.util.Result;
import com.cjc.vo.OrderPreviewVo;
import com.cjc.vo.OrderVo;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单控制器（Redis购物车版本）
 */
@Slf4j
@Tag(name = "订单管理", description = "订单相关接口")
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 订单预览
     * 参数变更：items格式为goodsId_itemId字符串列表
     */
    @Operation(summary = "订单预览")
    @PostMapping("/preview")
    public Result preview(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String userId = getUserId(request);

        List<String> items = (List<String>) params.get("items");
        if (items == null || items.isEmpty()) {
            throw new BusinessException("请选择要结算的商品");
        }

        OrderPreviewVo previewVo = orderService.preview(items, userId);
        return Result.success(previewVo);
    }

    /**
     * 创建订单
     */
    @Operation(summary = "创建订单")
    @PostMapping("/create")
    public Result create(@RequestBody OrderCreateDto dto, HttpServletRequest request) {
        String userId = getUserId(request);
        Long orderId = orderService.create(dto, userId);
        return Result.success(orderId.toString());
    }

    /**
     * 获取订单列表
     */
    @Operation(summary = "获取用户订单列表")
    @GetMapping("/list")
    public Result list(@RequestParam(required = false) String status, HttpServletRequest request) {
        String userId = getUserId(request);
        List<OrderVo> orders = orderService.list(userId, status);
        return Result.success(orders);
    }

    /**
     * 获取订单详情
     */
    @Operation(summary = "获取订单详情")
    @GetMapping("/{orderId}")
    public Result getById(@PathVariable Long orderId, HttpServletRequest request) {
        String userId = getUserId(request);
        OrderVo order = orderService.getById(orderId, userId);
        return Result.success(order);
    }

    /**
     * 取消订单
     */
    @Operation(summary = "取消订单")
    @PostMapping("/cancel/{orderId}")
    public Result cancel(@PathVariable Long orderId, HttpServletRequest request) {
        String userId = getUserId(request);
        orderService.cancel(orderId, userId);
        return Result.success("取消成功");
    }

    /**
     * 确认收货
     */
    @Operation(summary = "确认收货")
    @PostMapping("/confirm/{orderId}")
    public Result confirmReceive(@PathVariable Long orderId, HttpServletRequest request) {
        String userId = getUserId(request);
        orderService.confirmReceive(orderId, userId);
        return Result.success("确认成功");
    }

    /**
     * 删除订单
     */
    @Operation(summary = "删除订单")
    @DeleteMapping("/{orderId}")
    public Result delete(@PathVariable Long orderId, HttpServletRequest request) {
        String userId = getUserId(request);
        orderService.delete(orderId, userId);
        return Result.success("删除成功");
    }

    /**
     * 商家发货
     */
    @Operation(summary = "商家发货")
    @PostMapping("/seller/deliver/{orderId}")
    public Result deliver(@PathVariable Long orderId, HttpServletRequest request) {
        String sellerId = getSellerId(request);
        orderService.deliver(orderId, sellerId);
        return Result.success("发货成功");
    }

    /**
     * 商家订单列表
     */
    @Operation(summary = "商家订单列表")
    @GetMapping("/seller/list")
    public Result sellerList(@RequestParam(required = false) String status, HttpServletRequest request) {
        String sellerId = getSellerId(request);
        List<OrderVo> orders = orderService.sellerList(sellerId, status);
        return Result.success(orders);
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

    /**
     * 从请求中获取商家ID
     */
    private String getSellerId(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            throw new BusinessException("请先登录");
        }
        Claims claims = JwtUtil.parseJwt(token);
        return claims.getSubject();
    }
}