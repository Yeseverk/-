package com.cjc.dto;

import lombok.Data;

import java.util.Date;

/**
 * 用户信息更新 DTO
 */
@Data
public class UserUpdateDto {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 真实姓名
     */
    private String name;

    /**
     * 头像地址
     */
    private String headPic;

    /**
     * 性别，1男，2女
     */
    private String sex;

    /**
     * 生日
     */
    private Date birthday;

    /**
     * QQ号码
     */
    private String qq;

    /**
     * 邮箱
     */
    private String email;
}