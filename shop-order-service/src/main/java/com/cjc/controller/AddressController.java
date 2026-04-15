package com.cjc.controller;

import com.cjc.exception.BusinessException;
import com.cjc.pojo.TbAddress;
import com.cjc.service.AddressService;
import com.cjc.util.JwtUtil;
import com.cjc.util.Result;
import com.cjc.vo.AddressVo;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "收货地址管理", description = "收货地址相关接口")
@RestController
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    /**
     * 获取地址列表
     */
    @Operation(summary = "获取用户地址列表")
    @GetMapping("/list")
    public Result list(HttpServletRequest request) {
        String userId = getUserId(request);
        List<AddressVo> addresses = addressService.list(userId);
        return Result.success(addresses);
    }

    /**
     * 获取地址详情
     */
    @Operation(summary = "获取地址详情")
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id, HttpServletRequest request) {
        String userId = getUserId(request);
        AddressVo address = addressService.getById(id, userId);
        return Result.success(address);
    }

    /**
     * 新增地址
     */
    @Operation(summary = "新增收货地址")
    @PostMapping("/add")
    public Result add(@RequestBody TbAddress address, HttpServletRequest request) {
        String userId = getUserId(request);
        address.setUserId(userId);
        addressService.add(address);
        return Result.success("添加成功");
    }

    /**
     * 修改地址
     */
    @Operation(summary = "修改收货地址")
    @PostMapping("/update")
    public Result update(@RequestBody TbAddress address, HttpServletRequest request) {
        String userId = getUserId(request);
        addressService.update(address, userId);
        return Result.success("修改成功");
    }

    /**
     * 删除地址
     */
    @Operation(summary = "删除收货地址")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id, HttpServletRequest request) {
        String userId = getUserId(request);
        addressService.delete(id, userId);
        return Result.success("删除成功");
    }

    /**
     * 设置默认地址
     */
    @Operation(summary = "设置默认地址")
    @PostMapping("/setDefault/{id}")
    public Result setDefault(@PathVariable Long id, HttpServletRequest request) {
        String userId = getUserId(request);
        addressService.setDefault(id, userId);
        return Result.success("设置成功");
    }

    /**
     * 获取默认地址
     */
    @Operation(summary = "获取用户默认地址")
    @GetMapping("/default")
    public Result getDefault(HttpServletRequest request) {
        String userId = getUserId(request);
        AddressVo address = addressService.getDefault(userId);
        return Result.success(address);
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