package com.cjc.api;

import com.cjc.pojo.TbUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户服务 Feign 接口
 */
@FeignClient("shop-user-service")
public interface UserApi {

    /**
     * 根据用户名查询用户
     */
    @GetMapping("/user/queryByUsername")
    TbUser queryByUsername(@RequestParam String username);

    /**
     * 用户注册
     */
    @PostMapping("/user/register")
    void register(@RequestBody TbUser user);
}