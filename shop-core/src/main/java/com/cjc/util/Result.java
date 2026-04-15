package com.cjc.util;

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

    // 静态工厂方法
    public static <T> Result<T> success(T data) {
        return new Result<>("10000", "成功", data);
    }

    public static <T> Result<T> success(String message) {
        return new Result<>("10000", message, null);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>("-1", message, null);
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
