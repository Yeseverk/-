package com.cjc.filter;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.cjc.util.JwtUtil;
import com.cjc.util.Result;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component // 交个spring容器了
@Order(-1)  // 执行顺序，数字越小，优先级越高
public class LoginGlobalFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        // 放行路径：登录、注册、文件、支付、前台商品浏览
        String[] arr = {
            "/login/**",
            "/seller/register",
            "/admin/register",
            "/user/register",
            "/file/**",
            "/pay/**",
            "/user/sendRegisterCode",
            "/goods/list/**",       // 前台商品列表
            "/goods/detail/**",     // 前台商品详情
            "/goods/admin/detail/**", // 运营商查看商品详情
            "/item/cat/**",         // 分类查询
            "/brand/**",            // 品牌查询
            "/admin/content/queryByCategoryId",  // 前台广告查询（按分类ID）
            "/admin/content/queryByKey"          // 前台广告查询（按分类KEY）
        };
        // 1. 获取请求路径，判断是否是登陆请求。
        ServerHttpRequest request = exchange.getRequest();
        String uri = request.getURI().getPath(); // /login/adminLogin
        // 循环  判断是否是登陆操作
        for (String s : arr) {
            if(antPathMatcher.match(s, uri)){
                return chain.filter(exchange);
            }
        }
        // 不是登陆登陆请求， 获取请求头的token，解析。
        String jwtToken = request.getHeaders().getFirst("token");
        System.out.println("========== Gateway 拦截 ==========");
        System.out.println("请求路径: " + uri);
        System.out.println("Token: " + (jwtToken != null ? jwtToken.substring(0, Math.min(50, jwtToken.length())) + "..." : "null"));
        
        if(StringUtils.isEmpty(jwtToken)){
            System.out.println("Token 为空，拒绝访问");
            return getVoidMono(exchange);
        }
        try {
            // 解析成功，token没有过期，之前 登陆过。直接放行。
            JwtUtil.parseJwt(jwtToken);
            System.out.println("Token 验证成功，放行");
        } catch (Exception e) {
            // 解析不成功。token无效，反馈token无效信息
            System.out.println("Token 解析失败: " + e.getClass().getName() + " - " + e.getMessage());
            return getVoidMono(exchange);
        }
        // 放行
        return chain.filter(exchange);
    }
    private Mono<Void> getVoidMono(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        //设置响应码,我要自己处理
        response.setStatusCode(HttpStatus.OK);
        //设置响应头编码
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        //设置响应内容
        Result result = new Result("-10000","未登录");
        //不输出为null的字段
        byte[] bytes = JSONObject.toJSONBytes(result, SerializerFeature.WriteMapNullValue);
        DataBuffer wrap = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(wrap));
    }
}
