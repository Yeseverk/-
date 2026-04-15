package com.cjc.api;

import com.cjc.pojo.TbAdmin;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("shop-admin-service") // 服务名
public interface AdminApi {

    @GetMapping("/admin/queryByUsername")
    TbAdmin queryByUsername(@RequestParam String username);
}
