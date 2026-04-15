package com.cjc.exception;

import com.cjc.util.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理类
 */
@RestControllerAdvice
public class GlobalException {


    /**
     * 自定义异常类处理
     * @param e
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public Result handleBusinessException(BusinessException e){
        e.printStackTrace();
        return new Result("500", e.getMessage());
    }

    /**
     * 统一处理异常 Exception
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e){
        e.printStackTrace();
        return new Result("500", "服务器异常");
    }
}
