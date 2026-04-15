package com.cjc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.cjc.mapper")
public class AdminApp {

    public static void main(String[] args) {
        SpringApplication.run(AdminApp.class, args);
    }
}
