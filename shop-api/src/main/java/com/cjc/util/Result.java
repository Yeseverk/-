package com.cjc.util;

import com.cjc.constants.ResultCode;

public class Result<T> {

    private String code;
    private String message;
    private T data;

    public Result(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Result() {
    }

    public Result(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public Result(ResultCode resultCode) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    /**
     * 返回成功的结果
     * @return
     *
     */
    public static <T> Result<T> success(T data){
        return new Result<T>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success(){
        return new Result<T>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
    }

    /**
     * 返回失败的结果
     * @return
     */
    public static <T> Result<T> fail(T data){
        return new Result<T>(ResultCode.FAIL.getCode(), ResultCode.FAIL.getMessage());
    }

    public static <T> Result<T> fail(ResultCode resultCode){
        return new Result<T>(resultCode.getCode(), resultCode.getMessage());
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
