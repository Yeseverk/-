package com.cjc.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 阿里云市场短信服务工具类（国阳云）
 *
 * @author 航哥
 */
@Component
public class SmsUtil {

    private static final Logger log = LoggerFactory.getLogger(SmsUtil.class);

    /** 国阳云短信服务地址 */
    private static final String HOST = "https://gyytz.market.alicloudapi.com";
    private static final String PATH = "/sms/smsSend";
    private static final String METHOD = "POST";

    /** 成功状态码 */
    private static final String SUCCESS_CODE = "10000";

    /** AppCode - 从配置文件读取 */
    @Value("${sms.aliyun.appcode:}")
    private String appcode;

    /** 短信签名ID - 从配置文件读取 */
    @Value("${sms.aliyun.sms-sign-id:}")
    private String smsSignId;

    /** 短信模板ID - 从配置文件读取 */
    @Value("${sms.aliyun.template-id:}")
    private String templateId;

    /** 验证码有效期（分钟） */
    @Value("${sms.verify-code.expire-minutes:5}")
    private int expireMinutes;

    /**
     * 发送验证码短信
     *
     * @param phoneNumber 手机号
     * @return Result，成功时 data 为验证码
     */
    public Result<String> sendVerifyCode(String phoneNumber) {
        // 生成6位随机验证码
        String verifyCode = generateVerifyCode(6);

        // 发送短信
        Result<String> result = sendSms(phoneNumber, verifyCode);

        // 如果发送成功，把验证码放在 data 中返回
        if (SUCCESS_CODE.equals(result.getCode())) {
            result.setData(verifyCode);
        }

        return result;
    }

    /**
     * 发送短信
     *
     * @param phoneNumber 手机号
     * @param code        验证码
     * @return Result
     */
    public Result<String> sendSms(String phoneNumber, String code) {
        log.info("发送短信到手机: {}, 验证码: {}", phoneNumber, code);

        // 参数校验
        if (phoneNumber == null || phoneNumber.length() != 11) {
            return new Result<>("-1", "手机号格式不正确");
        }
        if (appcode == null || appcode.isEmpty()) {
            log.error("阿里云短信 AppCode 未配置");
            return new Result<>("-1", "短信服务未配置");
        }
        if (smsSignId == null || smsSignId.isEmpty()) {
            log.error("短信签名ID未配置");
            return new Result<>("-1", "短信签名未配置");
        }
        if (templateId == null || templateId.isEmpty()) {
            log.error("短信模板ID未配置");
            return new Result<>("-1", "短信模板未配置");
        }

        try {
            // 构建请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "APPCODE " + appcode);

            // 构建查询参数（国阳云用 query 参数）
            Map<String, String> querys = new HashMap<>();
            querys.put("mobile", phoneNumber);
            querys.put("param", "**code**:" + code + ",**minute**:" + expireMinutes);
            querys.put("smsSignId", smsSignId);
            querys.put("templateId", templateId);

            // 发送请求
            HttpResponse response = HttpUtils.doPost(HOST, PATH, METHOD, headers, querys, (Map<String, String>) null);
            String result = EntityUtils.toString(response.getEntity());

            log.info("短信发送响应: {}", result);

            // 检查响应是否为空
            if (result == null || result.isEmpty()) {
                log.error("短信 API 返回为空");
                return new Result<>("-1", "短信服务响应异常");
            }

            // 解析响应
            JSONObject jsonResult = JSON.parseObject(result);
            if (jsonResult == null) {
                log.error("短信 API 返回格式错误: {}", result);
                return new Result<>("-1", "短信服务响应格式错误");
            }

            // 国阳云返回格式: {"code":"0","msg":"成功"} 成功
            String respCode = jsonResult.getString("code");
            String msg = jsonResult.getString("msg");
            
            if ("0".equals(respCode)) {
                log.info("短信发送成功: {}", phoneNumber);
                return new Result<>(SUCCESS_CODE, "发送成功");
            } else {
                log.warn("短信发送失败: {}, 原因: {}", phoneNumber, msg);
                return new Result<>("-1", msg != null ? msg : "发送失败");
            }

        } catch (Exception e) {
            log.error("短信发送异常: {}", phoneNumber, e);
            return new Result<>("-1", "短信发送异常: " + e.getMessage());
        }
    }

    /**
     * 生成指定长度的随机验证码
     */
    private String generateVerifyCode(int length) {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}