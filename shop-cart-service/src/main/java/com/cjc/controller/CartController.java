package com.cjc.controller;

import com.cjc.service.CartService;
import com.cjc.util.JwtUtil;
import com.cjc.util.Result;
import com.cjc.vo.CartVo;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 购物车控制器（Redis版本）
 * 使用goodsId+itemId定位商品，不再使用cartId
 */
@Slf4j
@Tag(name = "购物车管理", description = "购物车相关接口（Redis版本）")
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 获取购物车列表（含商品详情）
     */
    @Operation(summary = "获取购物车列表")
    @GetMapping("/list")
    public Result list(HttpServletRequest request) {
        Long userId = getUserId(request);
        List<CartVo> carts = cartService.listWithGoods(userId);
        return Result.success(carts);
    }

    /**
     * 添加到购物车
     */
    @Operation(summary = "添加商品到购物车")
    @PostMapping("/add")
    public Result add(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserId(request);
        Long goodsId = Long.parseLong(params.get("goodsId").toString());
        Long itemId = params.get("itemId") != null ? Long.parseLong(params.get("itemId").toString()) : null;
        Integer num = params.get("num") != null ? Integer.parseInt(params.get("num").toString()) : 1;

        cartService.add(userId, goodsId, itemId, num);
        return Result.success("添加成功");
    }

    /**
     * 更新数量
     * 参数变更：不再使用cartId，改用goodsId+itemId
     */
    @Operation(summary = "更新购物车商品数量")
    @PostMapping("/updateNum")
    public Result updateNum(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserId(request);
        Long goodsId = Long.parseLong(params.get("goodsId").toString());
        Long itemId = params.get("itemId") != null ? Long.parseLong(params.get("itemId").toString()) : null;
        Integer num = Integer.parseInt(params.get("num").toString());

        cartService.updateNum(userId, goodsId, itemId, num);
        return Result.success("更新成功");
    }

    /**
     * 删除商品
     * 参数变更：不再使用cartId，改用goodsId+itemId
     */
    @Operation(summary = "删除购物车商品")
    @DeleteMapping("/delete/{goodsId}/{itemId}")
    public Result delete(@PathVariable Long goodsId, @PathVariable Long itemId, HttpServletRequest request) {
        Long userId = getUserId(request);
        cartService.delete(userId, goodsId, itemId);
        return Result.success("删除成功");
    }

    /**
     * 批量删除商品
     * 参数变更：传入商品列表 [{goodsId, itemId}]
     */
    @Operation(summary = "批量删除购物车商品")
    @PostMapping("/batchDelete")
    public Result batchDelete(@RequestBody List<Map<String, Object>> items, HttpServletRequest request) {
        Long userId = getUserId(request);

        List<String> fields = new ArrayList<>();
        for (Map<String, Object> item : items) {
            Long goodsId = Long.parseLong(item.get("goodsId").toString());
            Long itemId = item.get("itemId") != null ? Long.parseLong(item.get("itemId").toString()) : 0L;
            fields.add(goodsId + "_" + itemId);
        }

        cartService.batchDelete(userId, fields);
        return Result.success("删除成功");
    }

    /**
     * 获取购物车数量
     */
    @Operation(summary = "获取购物车商品数量")
    @GetMapping("/count")
    public Result count(HttpServletRequest request) {
        Long userId = getUserId(request);
        Integer count = cartService.count(userId);
        return Result.success(count);
    }

    /**
     * 清空购物车
     */
    @Operation(summary = "清空购物车")
    @DeleteMapping("/clear")
    public Result clear(HttpServletRequest request) {
        Long userId = getUserId(request);
        cartService.clear(userId);
        return Result.success("清空成功");
    }

    /**
     * 从请求中获取用户ID
     */
    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("请先登录");
        }
        Claims claims = JwtUtil.parseJwt(token);
        return Long.parseLong(claims.getId());
    }
}