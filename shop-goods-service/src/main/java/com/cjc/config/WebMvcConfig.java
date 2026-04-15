package com.cjc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${static.page.path:D:/static/goods}")
    private String staticPagePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态页访问路径：/static/goods/** -> D:/static/goods/
        registry.addResourceHandler("/static/goods/**")
                .addResourceLocations("file:" + staticPagePath + "/");
    }
}
