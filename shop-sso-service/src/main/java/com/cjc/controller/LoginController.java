package com.cjc.controller;

import com.alibaba.fastjson.JSONObject;
import com.cjc.constants.LoginConstants;
import com.cjc.constants.ResultCode;
import com.cjc.dto.LoginDto;
import com.cjc.pojo.TbAdmin;
import com.cjc.pojo.TbSeller;
import com.cjc.pojo.TbUser;
import com.cjc.usernamepasswordtoken.LoginUsernamePasswordToken;
import com.cjc.util.JwtUtil;
import com.cjc.util.Result;
import com.cjc.util.SmsUtil;
import com.cjc.vo.TbSellerVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class LoginController {

    /**
     * admin 登录
     */
    @PostMapping("/adminLogin")
    public Result adminLogin(@RequestBody LoginDto loginDto) {

        // 校验
        String username = loginDto.getUsername();
        String password = loginDto.getPassword();
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            return Result.fail(ResultCode.USERNAME_PASSWORD_NOT_NULL);
        }

        // 2. 获取subject
        Subject subject = SecurityUtils.getSubject();

        // 3. 封装数据
        LoginUsernamePasswordToken usernamePasswordToken = new LoginUsernamePasswordToken(username, password,
                LoginConstants.ADMIN);
        try {
            subject.login(usernamePasswordToken);
        } catch (UnknownAccountException e) {
            return Result.fail(ResultCode.USERNAME_NOT_EXIST);
        } catch (IncorrectCredentialsException e) {
            return Result.fail(ResultCode.USERNAME_PASSWORD_ERROR);
        }

        // 获取登录人信息
        TbAdmin admin = (TbAdmin) subject.getPrincipal();

        // 脱敏操作
        admin.setPasssword("");
        admin.setSalt("");

        // 登陆成功， 生成jwt的token
        String token = JwtUtil.createJwt(admin.getId().toString(), JSONObject.toJSONString(admin), "adminRoles");
        return Result.success(token);
    }

    /**
     * seller 登录
     */
    @PostMapping("/sellerLogin")
    public Result sellerLogin(@RequestBody LoginDto loginDto) {

        // 校验
        String username = loginDto.getUsername();
        String password = loginDto.getPassword();
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            return Result.fail(ResultCode.USERNAME_PASSWORD_NOT_NULL);
        }

        // 2. 获取subject
        Subject subject = SecurityUtils.getSubject();

        // 3. 封装数据
        LoginUsernamePasswordToken usernamePasswordToken = new LoginUsernamePasswordToken(username, password,
                LoginConstants.SELLER);
        try {
            subject.login(usernamePasswordToken);
        } catch (UnknownAccountException e) {
            return Result.fail(ResultCode.USERNAME_NOT_EXIST);
        } catch (IncorrectCredentialsException e) {
            return Result.fail(ResultCode.USERNAME_PASSWORD_ERROR);
        }

        // 获取登录人信息
        TbSeller seller = (TbSeller) subject.getPrincipal();

        TbSellerVo tbSellerVo = new TbSellerVo();
        BeanUtils.copyProperties(seller, tbSellerVo);
        // 脱敏操作
        tbSellerVo.setPassword("");
        tbSellerVo.setSalt("");

        // 登陆成功， 生成jwt的token
        String token = JwtUtil.createJwt(tbSellerVo.getSellerId(), JSONObject.toJSONString(tbSellerVo), "sellerRoles");
        return Result.success(token);
    }

    /**
     * user 用户登录（前台用户）
     */
    @PostMapping("/userLogin")
    public Result userLogin(@RequestBody LoginDto loginDto) {

        // 校验
        String username = loginDto.getUsername();
        String password = loginDto.getPassword();
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            return Result.fail(ResultCode.USERNAME_PASSWORD_NOT_NULL);
        }

        // 2. 获取subject
        Subject subject = SecurityUtils.getSubject();

        // 3. 封装数据
        LoginUsernamePasswordToken usernamePasswordToken = new LoginUsernamePasswordToken(username, password,
                LoginConstants.USER);
        try {
            subject.login(usernamePasswordToken);
        } catch (UnknownAccountException e) {
            return Result.fail(ResultCode.USERNAME_NOT_EXIST);
        } catch (IncorrectCredentialsException e) {
            return Result.fail(ResultCode.USERNAME_PASSWORD_ERROR);
        }

        // 获取登录人信息
        TbUser user = (TbUser) subject.getPrincipal();

        // 脱敏操作
        user.setPassword("");
        user.setSalt("");

        // 登陆成功， 生成jwt的token
        String token = JwtUtil.createJwt(user.getId().toString(), JSONObject.toJSONString(user), "userRoles");
        return Result.success(token);
    }

}
