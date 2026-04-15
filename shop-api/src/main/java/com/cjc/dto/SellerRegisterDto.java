package com.cjc.dto;

import lombok.Data;

@Data
public class SellerRegisterDto {
    // 账号信息
    private String sellerId;    // 登录用户名（对应数据库 sellerId）
    private String password;
    // 公司信息
    private String name;
    private String mobile;
    private String telephone;
    private String addressDetail;
    // 联系人信息
    private String nickName;
    private String linkmanQq;
    private String linkmanMobile;
    private String linkmanEmail;
    // 资质信息
    private String licenseNumber;
    private String taxNumber;
    private String orgNumber;
    // 法人信息
    private String legalPerson;
    private String legalPersonCardId;
    // 银行账户
    private String bankName;
    private String bankUser;
    // 注意：confirmPassword 前端验证用，后端不需要
}