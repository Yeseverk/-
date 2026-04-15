package com.cjc.api;

import com.cjc.pojo.TbSeller;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("shop-seller-service") // 服务名
public interface SellerApi {

    @GetMapping("/seller/queryByUsername")
    TbSeller queryByUsername(@RequestParam String username);
}
