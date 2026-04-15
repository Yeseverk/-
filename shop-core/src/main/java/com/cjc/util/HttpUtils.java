package com.cjc.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 阿里云 API 网关 HTTP 工具类
 * 
 * 来源：阿里云市场官方 Demo
 * https://github.com/aliyun/api-gateway-demo-sign-java
 *
 * @author 阿里云
 */
public class HttpUtils {

    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);

    /**
     * 发送 GET 请求
     *
     * @param host    请求地址
     * @param path    请求路径
     * @param method  请求方法
     * @param headers 请求头
     * @param querys  查询参数
     * @return HttpResponse
     */
    public static HttpResponse doGet(String host, String path, String method,
                                     Map<String, String> headers,
                                     Map<String, String> querys) throws Exception {
        HttpClient httpClient = wrapClient(host);

        HttpGet request = new HttpGet(buildUrl(host, path, querys));
        for (Map.Entry<String, String> e : headers.entrySet()) {
            request.addHeader(e.getKey(), e.getValue());
        }
        return httpClient.execute(request);
    }

    /**
     * 发送 POST 请求（表单形式）
     *
     * @param host    请求地址
     * @param path    请求路径
     * @param method  请求方法
     * @param headers 请求头
     * @param querys  查询参数
     * @param bodys   请求体参数
     * @return HttpResponse
     */
    public static HttpResponse doPost(String host, String path, String method,
                                       Map<String, String> headers,
                                       Map<String, String> querys,
                                       Map<String, String> bodys) throws Exception {
        HttpClient httpClient = wrapClient(host);

        HttpPost request = new HttpPost(buildUrl(host, path, querys));
        for (Map.Entry<String, String> e : headers.entrySet()) {
            request.addHeader(e.getKey(), e.getValue());
        }

        if (bodys != null) {
            List<NameValuePair> nameValuePairList = new ArrayList<>();
            for (Map.Entry<String, String> entry : bodys.entrySet()) {
                nameValuePairList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nameValuePairList, StandardCharsets.UTF_8);
            formEntity.setContentType("application/x-www-form-urlencoded; charset=UTF-8");
            request.setEntity(formEntity);
        }

        return httpClient.execute(request);
    }

    /**
     * 发送 POST 请求（JSON 形式）
     *
     * @param host    请求地址
     * @param path    请求路径
     * @param method  请求方法
     * @param headers 请求头
     * @param querys  查询参数
     * @param body    JSON 字符串请求体
     * @return HttpResponse
     */
    public static HttpResponse doPost(String host, String path, String method,
                                       Map<String, String> headers,
                                       Map<String, String> querys,
                                       String body) throws Exception {
        HttpClient httpClient = wrapClient(host);

        HttpPost request = new HttpPost(buildUrl(host, path, querys));
        for (Map.Entry<String, String> e : headers.entrySet()) {
            request.addHeader(e.getKey(), e.getValue());
        }

        if (StringUtils.isNotBlank(body)) {
            request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        }

        return httpClient.execute(request);
    }

    /**
     * 发送 PUT 请求
     *
     * @param host    请求地址
     * @param path    请求路径
     * @param method  请求方法
     * @param headers 请求头
     * @param querys  查询参数
     * @param body    请求体
     * @return HttpResponse
     */
    public static HttpResponse doPut(String host, String path, String method,
                                      Map<String, String> headers,
                                      Map<String, String> querys,
                                      String body) throws Exception {
        HttpClient httpClient = wrapClient(host);

        HttpPut request = new HttpPut(buildUrl(host, path, querys));
        for (Map.Entry<String, String> e : headers.entrySet()) {
            request.addHeader(e.getKey(), e.getValue());
        }

        if (StringUtils.isNotBlank(body)) {
            request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        }

        return httpClient.execute(request);
    }

    /**
     * 发送 DELETE 请求
     *
     * @param host    请求地址
     * @param path    请求路径
     * @param method  请求方法
     * @param headers 请求头
     * @param querys  查询参数
     * @return HttpResponse
     */
    public static HttpResponse doDelete(String host, String path, String method,
                                         Map<String, String> headers,
                                         Map<String, String> querys) throws Exception {
        HttpClient httpClient = wrapClient(host);

        HttpDelete request = new HttpDelete(buildUrl(host, path, querys));
        for (Map.Entry<String, String> e : headers.entrySet()) {
            request.addHeader(e.getKey(), e.getValue());
        }

        return httpClient.execute(request);
    }

    /**
     * 构建完整 URL
     */
    private static String buildUrl(String host, String path, Map<String, String> querys) throws UnsupportedEncodingException {
        StringBuilder sbUrl = new StringBuilder();
        sbUrl.append(host);
        if (StringUtils.isNotBlank(path)) {
            sbUrl.append(path);
        }
        if (null != querys) {
            StringBuilder sbQuery = new StringBuilder();
            for (Map.Entry<String, String> query : querys.entrySet()) {
                if (sbQuery.length() > 0) {
                    sbQuery.append("&");
                }
                if (StringUtils.isBlank(query.getKey()) && StringUtils.isNotBlank(query.getValue())) {
                    sbQuery.append(query.getValue());
                }
                if (StringUtils.isNotBlank(query.getKey())) {
                    sbQuery.append(query.getKey());
                    if (StringUtils.isNotBlank(query.getValue())) {
                        sbQuery.append("=");
                        sbQuery.append(URLEncoder.encode(query.getValue(), "UTF-8"));
                    }
                }
            }
            if (sbQuery.length() > 0) {
                sbUrl.append("?").append(sbQuery);
            }
        }
        return sbUrl.toString();
    }

    /**
     * 创建 HttpClient（支持 HTTPS）
     */
    private static HttpClient wrapClient(String host) {
        HttpClient httpClient = HttpClients.createDefault();
        if (host.startsWith("https://")) {
            return createSSLClientDefault();
        }
        return httpClient;
    }

    /**
     * 创建支持 SSL 的 HttpClient（信任所有证书）
     */
    private static CloseableHttpClient createSSLClientDefault() {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) {
                    return true;
                }
            }).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
            return HttpClients.custom().setSSLSocketFactory(sslsf).build();
        } catch (Exception e) {
            log.error("创建 SSL HttpClient 失败", e);
        }
        return HttpClients.createDefault();
    }
}