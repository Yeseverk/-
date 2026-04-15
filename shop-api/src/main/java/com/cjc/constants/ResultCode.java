package com.cjc.constants;

public enum ResultCode {

    SUCCESS("10000", "操作成功"),
    FAIL("10001", "操作失败"),

    // 用户名或密码不能为空
    USERNAME_PASSWORD_NOT_NULL("10002", "用户名或密码不能为空"),

    // 用户名不存在
    USERNAME_NOT_EXIST("10003", "用户名不存在"),
    USERNAME_PASSWORD_ERROR("10004","密码错误");

    private String code;
    private String message;

    ResultCode(String code, String message) {
        this.code = code;
        this.message = message;
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
}
