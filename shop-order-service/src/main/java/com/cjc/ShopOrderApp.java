package com.cjc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.cjc.mapper")
public class ShopOrderApp {
    public static void main(String[] args) {
        SpringApplication.run(ShopOrderApp.class, args);
    }
}