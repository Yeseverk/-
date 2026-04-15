package com.cjc.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 百度云内容审核配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "xingyuhang.baidu.audit")
public class BaiduAuditProperties {
    private String appId;
    private String apiKey;
    private String secretKey;
}
