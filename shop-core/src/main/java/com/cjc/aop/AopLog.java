package com.cjc.aop;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j // 日志
public class AopLog {

    /**
     *   *: 返回值
     *   ..: 方法中的参数
     */
    @Pointcut("execution(public * com.cjc.service.impl.*.*(..)) || @annotation(com.cjc.aop.AnnoLog)")
    public void pointCut(){}

    // 前置通知
    @Before("pointCut()")
    public void before(JoinPoint joinPoint){
        // 打印方法名，和方法的参数
        Object[] args = joinPoint.getArgs();
        // 方法名
        Signature signature = joinPoint.getSignature();
        log.info("方法名：{}，方法参数为：{}", signature.getName(), args);
    }

    // 后置通知
    @AfterReturning(value = "pointCut()", returning = "result")
    public void afterReturning(Object result){
        log.info("返回值为："+ JSONObject.toJSONString( result));
    }
}
