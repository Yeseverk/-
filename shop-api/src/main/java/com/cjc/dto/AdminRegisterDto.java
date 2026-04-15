package com.cjc.dto;

import lombok.Data;

@Data
public class AdminRegisterDto {
    private String username;    // 登录用户名
    private String password;    // 密码
    private String nickName;    // 昵称（可选）
    private String phone;       // 手机号（可选）
    private String email;       // 邮箱（可选）
}