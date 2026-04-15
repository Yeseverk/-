package com.cjc.api;

import com.cjc.pojo.TbCart;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * 购物车服务 Feign 接口
 */
@FeignClient("shop-cart-service")
public interface CartApi {

    /**
     * 获取购物车列表
     */
    @GetMapping("/cart/list")
    List<TbCart> list(@RequestBody Map<String, Object> params);

    /**
     * 获取购物车数量
     */
    @GetMapping("/cart/count")
    Integer count(@RequestBody Map<String, Object> params);

    /**
     * 删除购物车商品
     */
    @DeleteMapping("/cart/delete/{cartId}")
    void delete(@PathVariable Long cartId, @RequestBody Map<String, Object> params);
}