package com.cjc.service;

import com.cjc.dto.UserRegisterDto;
import com.cjc.dto.UserUpdateDto;
import com.cjc.pojo.TbUser;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 根据用户名查询用户
     */
    TbUser queryByUsername(String username);

    /**
     * 根据ID查询用户
     */
    TbUser queryById(Long userId);

    /**
     * 用户注册
     */
    void register(UserRegisterDto userdto);

    /**
     * 发送手机验证码
     * @param phone
     */
    void sendRegisterCode(String phone);

    /**
     * 更新用户信息
     */
    void updateUser(UserUpdateDto userDto);

    /**
     * 发送短信验证码（用于绑定手机）
     */
    void sendSmsCode(String phone);

    /**
     * 发送邮箱验证码（用于绑定邮箱）
     */
    void sendEmailCode(String email);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 绑定手机
     */
    void bindPhone(Long userId, String phone, String code);

    /**
     * 绑定邮箱
     */
    void bindEmail(Long userId, String email, String code);

    /**
     * 注销账号
     */
    void deleteAccount(Long userId);
}