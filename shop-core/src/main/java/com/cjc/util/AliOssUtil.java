package com.cjc.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.ObjectMetadata;
import com.cjc.exception.BusinessException;
import com.cjc.properties.AliOssProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class AliOssUtil {

    @Autowired
    private AliOssProperties properties;
    
    // 静态网站域名（开启OSS静态网站托管后使用此域名访问HTML）
    private static final String STATIC_WEBSITE_HOST = "yuhang-library-oss.oss-cn-hangzhou.aliyuncs.com";
    
    // 【核心黑客指令】：强行关闭 JAXB 的底层字节码优化，直接绕过 JDK 17 的 Unsafe 限制！
    static {
        System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");
    }
    
    /**
     * 文件上传核心方法
     */
    public String upload(byte[] bytes, String objectName) {
        // 生成防止重名的 UUID 文件名
        String fileName = UUID.randomUUID().toString() + objectName.substring(objectName.lastIndexOf("."));
        // 每次请求构建一个新的 OSSClient（V1 标准用法）
        OSS ossClient = new OSSClientBuilder().build(
                properties.getEndpoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret()
        );
        try {
            // 上传文件流
            ossClient.putObject(properties.getBucketName(), fileName, new ByteArrayInputStream(bytes));
            log.info("图片已成功上传");
        } catch (Exception e) {
            log.error("图片上传失败: {}", e.getMessage());
            throw new BusinessException("文件上传失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        // 拼接公网访问 URL
        return "https://" + properties.getBucketName() + "." + properties.getEndpoint() + "/" + fileName;
    }

    /**
     * 上传文件（自定义文件名/路径）
     * 用于静态页等需要固定路径的场景
     * @param bytes 文件内容
     * @param customPath 自定义路径，如 "static/goods/123.html"
     * @return 文件完整URL
     */
    public String uploadWithPath(byte[] bytes, String customPath) {
        OSS ossClient = new OSSClientBuilder().build(
                properties.getEndpoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret()
        );
        try {
            // 根据文件扩展名设置 Content-Type
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(getContentType(customPath));
            metadata.setContentLength(bytes.length);
            
            ossClient.putObject(properties.getBucketName(), customPath, new ByteArrayInputStream(bytes), metadata);
            log.info("文件上传成功: {}", customPath);
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage());
            throw new BusinessException("文件上传失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return "https://" + properties.getBucketName() + "." + properties.getEndpoint() + "/" + customPath;
    }
    
    /**
     * 根据文件扩展名获取 Content-Type
     */
    private String getContentType(String fileName) {
        if (fileName == null) {
            return "application/octet-stream";
        }
        
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".html") || lowerName.endsWith(".htm")) {
            return "text/html; charset=UTF-8";
        } else if (lowerName.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        } else if (lowerName.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        } else if (lowerName.endsWith(".json")) {
            return "application/json; charset=UTF-8";
        } else if (lowerName.endsWith(".xml")) {
            return "application/xml; charset=UTF-8";
        } else if (lowerName.endsWith(".png")) {
            return "image/png";
        } else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerName.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (lowerName.endsWith(".ico")) {
            return "image/x-icon";
        } else if (lowerName.endsWith(".woff") || lowerName.endsWith(".woff2")) {
            return "font/woff2";
        } else if (lowerName.endsWith(".ttf")) {
            return "font/ttf";
        }
        
        return "application/octet-stream";
    }
    
    /**
     * 删除 OSS 单个文件
     * @param url 文件完整 URL，如 https://bucket.endpoint/fileName
     */
    public void delete(String url) {
        if (url == null || url.isEmpty()) {
            return;
        }
        
        // 从 URL 中提取文件名（objectKey）
        String objectKey = extractObjectKey(url);
        if (objectKey == null) {
            log.warn("无法从 URL 提取文件名: {}", url);
            return;
        }
        
        OSS ossClient = new OSSClientBuilder().build(
                properties.getEndpoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret()
        );
        try {
            ossClient.deleteObject(properties.getBucketName(), objectKey);
            log.info("OSS 文件删除成功: {}", objectKey);
        } catch (Exception e) {
            log.error("OSS 文件删除失败: {}", e.getMessage());
            // 删除失败不抛异常，避免影响业务流程
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
    
    /**
     * 批量删除 OSS 文件
     * @param urls 文件 URL 列表
     */
    public void deleteBatch(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return;
        }
        
        List<String> objectKeys = new ArrayList<>();
        for (String url : urls) {
            String key = extractObjectKey(url);
            if (key != null) {
                objectKeys.add(key);
            }
        }
        
        if (objectKeys.isEmpty()) {
            return;
        }
        
        OSS ossClient = new OSSClientBuilder().build(
                properties.getEndpoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret()
        );
        try {
            // OSS SDK V1: 先创建 request，再设置 keys
            DeleteObjectsRequest request = new DeleteObjectsRequest(properties.getBucketName());
            request.setKeys(objectKeys);
            ossClient.deleteObjects(request);
            log.info("OSS 批量删除成功，共 {} 个文件", objectKeys.size());
        } catch (Exception e) {
            log.error("OSS 批量删除失败: {}", e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
    
    /**
     * 从富文本 HTML 中提取所有图片 URL
     * @param html 富文本内容
     * @return 图片 URL 列表
     */
    public List<String> extractImageUrls(String html) {
        if (html == null || html.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> urls = new ArrayList<>();
        // 匹配 <img src="xxx"> 或 <img src='xxx'>
        Pattern pattern = Pattern.compile("<img[^>]+src=[\"']([^\"']+)[\"']");
        Matcher matcher = pattern.matcher(html);
        
        while (matcher.find()) {
            String url = matcher.group(1);
            // 只提取属于我们 OSS 的图片 URL
            if (url != null && url.contains(properties.getBucketName())) {
                urls.add(url);
            }
        }
        
        return urls;
    }
    
    /**
     * 从完整 URL 中提取 OSS objectKey（文件名）
     * URL 格式: https://bucket.endpoint/fileName
     */
    private String extractObjectKey(String url) {
        if (url == null) {
            return null;
        }
        
        // OSS URL 格式: https://bucketName.endpoint/fileName
        String prefix = "https://" + properties.getBucketName() + "." + properties.getEndpoint() + "/";
        if (url.startsWith(prefix)) {
            return url.substring(prefix.length());
        }
        
        // 兜底：提取最后一个 / 之后的内容
        int lastSlash = url.lastIndexOf("/");
        if (lastSlash > 0 && lastSlash < url.length() - 1) {
            return url.substring(lastSlash + 1);
        }
        
        return null;
    }
}