package com.cjc.util;

import com.baidu.aip.contentcensor.AipContentCensor;
import com.baidu.aip.contentcensor.EImgType;
import com.cjc.properties.BaiduAuditProperties;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 百度云内容审核工具类
 * 每月免费5000次调用额度
 */
@Component
@Slf4j
public class BaiduAuditUtil {

    @Autowired
    private BaiduAuditProperties properties;

    // 懒加载客户端
    private volatile AipContentCensor client;

    private AipContentCensor getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = new AipContentCensor(
                            properties.getAppId(),
                            properties.getApiKey(),
                            properties.getSecretKey()
                    );
                    // 设置超时时间
                    client.setConnectionTimeoutInMillis(5000);
                    client.setSocketTimeoutInMillis(10000);
                }
            }
        }
        return client;
    }

    /**
     * 审核图片是否安全
     * @param imageUrl 图片公网URL
     * @return true: 安全 / false: 违规（涉黄、暴恐、广告等）
     */
    public boolean auditImage(String imageUrl) {
        try {
            log.info("========== 百度图片审核开始 ==========");
            log.info("百度图片审核 - 图片URL: {}", imageUrl);

            // 调用百度图片审核接口，使用URL方式
            JSONObject response = getClient().imageCensorUserDefined(imageUrl, EImgType.URL, null);

            log.info("百度图片审核 - 响应结果: {}", response.toString());

            // 判断审核结果
            String conclusion = response.optString("conclusion");
            if ("合规".equals(conclusion)) {
                log.info("========== 百度图片审核成功：内容安全 ==========");
                return true;
            } else if ("不合规".equals(conclusion)) {
                log.warn("========== 百度图片审核失败：检测到违规内容 ==========");
                log.warn("百度图片审核 - 违规原因: {}", response.optJSONArray("data"));
                return false;
            } else if ("疑似".equals(conclusion)) {
                // 疑似也视为不通过，需要人工复核
                log.warn("========== 百度图片审核：疑似违规，需人工复核 ==========");
                log.warn("百度图片审核 - 疑似原因: {}", response.optJSONArray("data"));
                return false;
            } else {
                // 其他情况（审核失败等），降级处理为放行
                log.error("========== 百度图片审核异常：未知结果 ==========");
                log.error("百度图片审核 - 返回结果: {}", conclusion);
                return true;
            }
        } catch (Exception e) {
            // 异常时降级处理为放行，不影响核心业务
            log.error("========== 百度图片审核异常 ==========");
            log.error("百度图片审核 - 异常信息: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 审核图片是否安全（字节数组方式）
     * @param imageBytes 图片字节数组
     * @return true: 安全 / false: 违规
     */
    public boolean auditImage(byte[] imageBytes) {
        try {
            log.info("========== 百度图片审核开始 ==========");
            log.info("百度图片审核 - 审核方式: 字节数组");

            JSONObject response = getClient().imageCensorUserDefined(imageBytes, null);

            log.info("百度图片审核 - 响应结果: {}", response.toString());

            String conclusion = response.optString("conclusion");
            if ("合规".equals(conclusion)) {
                log.info("========== 百度图片审核成功：内容安全 ==========");
                return true;
            } else if ("不合规".equals(conclusion)) {
                log.warn("========== 百度图片审核失败：检测到违规内容 ==========");
                log.warn("百度图片审核 - 违规原因: {}", response.optJSONArray("data"));
                return false;
            } else if ("疑似".equals(conclusion)) {
                log.warn("========== 百度图片审核：疑似违规，需人工复核 ==========");
                log.warn("百度图片审核 - 疑似原因: {}", response.optJSONArray("data"));
                return false;
            } else {
                log.error("========== 百度图片审核异常：未知结果 ==========");
                log.error("百度图片审核 - 返回结果: {}", conclusion);
                return true;
            }
        } catch (Exception e) {
            log.error("========== 百度图片审核异常 ==========");
            log.error("百度图片审核 - 异常信息: {}", e.getMessage());
            return true;
        }
    }
}
