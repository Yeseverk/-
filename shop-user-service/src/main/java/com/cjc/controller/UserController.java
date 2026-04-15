package com.cjc.controller;

import com.cjc.dto.UserRegisterDto;
import com.cjc.dto.UserUpdateDto;
import com.cjc.pojo.TbUser;
import com.cjc.service.UserService;
import com.cjc.util.AliOssUtil;
import com.cjc.util.BaiduAuditUtil;
import com.cjc.util.JwtUtil;
import com.cjc.util.Result;
import com.cjc.util.SmsUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "用户管理", description = "用户管理接口")
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private AliOssUtil aliOssUtil;
    @Autowired
    private BaiduAuditUtil baiduAuditUtil;

    /**
     * 根据用户名查询用户（供 SSO 调用）
     */
    @GetMapping("/queryByUsername")
    public TbUser queryByUsername(@RequestParam String username) {
        return userService.queryByUsername(username);
    }

    /**
     * 发送验证码
     * @param phone
     * @return
     */
    @GetMapping("/sendRegisterCode")
    public Result sendRegisterCode(@RequestParam String phone) {
        userService.sendRegisterCode(phone);
        return Result.success();
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody UserRegisterDto userdto) {
        userService.register(userdto);
        return Result.success("注册成功");
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result info(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            return Result.fail("未登录");
        }
        
        try {
            Claims claims = JwtUtil.parseJwt(token);
            Long userId = Long.parseLong(claims.getId());
            TbUser user = userService.queryById(userId);
            if (user != null) {
                // 脱敏
                user.setPassword(null);
                user.setSalt(null);
            }
            return Result.success(user);
        } catch (Exception e) {
            return Result.fail("token无效");
        }
    }

    /**
     * 上传头像
     */
    @PostMapping("/uploadAvatar")
    public Result uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            return Result.fail("未登录");
        }
        
        try {
            Claims claims = JwtUtil.parseJwt(token);
            Long userId = Long.parseLong(claims.getId());
            
            // 上传到 OSS
            String originalFilename = file.getOriginalFilename();
            String avatarUrl = aliOssUtil.upload(file.getBytes(), originalFilename);

            // 图片审核（百度云）
            boolean isSafe = baiduAuditUtil.auditImage(avatarUrl);
            if (!isSafe) {
                return Result.fail("头像涉嫌违规内容，请更换图片");
            }

            // 更新用户头像
            TbUser user = new TbUser();
            user.setId(userId);
            user.setHeadPic(avatarUrl);
            userService.updateUser(new UserUpdateDto() {{
                setId(userId);
                setHeadPic(avatarUrl);
            }});
            
            log.info("用户 {} 上传头像成功: {}", userId, avatarUrl);
            return Result.success(avatarUrl);
        } catch (IOException e) {
            log.error("头像上传失败", e);
            return Result.fail("头像上传失败");
        } catch (Exception e) {
            return Result.fail("token无效");
        }
    }

    /**
     * 更新用户信息
     */
    @PostMapping("/update")
    public Result update(@RequestBody UserUpdateDto userDto, HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            return Result.fail("未登录");
        }

        try {
            Claims claims = JwtUtil.parseJwt(token);
            Long userId = Long.parseLong(claims.getId());

            // 设置用户ID，防止修改他人信息
            userDto.setId(userId);
            userService.updateUser(userDto);

            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.fail("token无效");
        }
    }

    /**
     * 发送短信验证码（用于绑定手机）
     */
    @PostMapping("/sendSms")
    public Result sendSms(@RequestBody java.util.Map<String, String> params) {
        String phone = params.get("phone");
        if (phone == null || phone.isEmpty()) {
            return Result.fail("手机号不能为空");
        }
        userService.sendSmsCode(phone);
        return Result.success("验证码已发送");
    }

    /**
     * 发送邮箱验证码（用于绑定邮箱）
     */
    @PostMapping("/sendEmail")
    public Result sendEmail(@RequestBody java.util.Map<String, String> params) {
        String email = params.get("email");
        if (email == null || email.isEmpty()) {
            return Result.fail("邮箱不能为空");
        }
        userService.sendEmailCode(email);
        return Result.success("验证码已发送到邮箱");
    }

    /**
     * 修改密码
     */
    @PostMapping("/changePassword")
    public Result changePassword(@RequestBody java.util.Map<String, String> params, HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            return Result.fail("未登录");
        }

        try {
            Claims claims = JwtUtil.parseJwt(token);
            Long userId = Long.parseLong(claims.getId());

            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");

            if (oldPassword == null || newPassword == null) {
                return Result.fail("密码不能为空");
            }

            userService.changePassword(userId, oldPassword, newPassword);
            return Result.success("密码修改成功");
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return Result.fail(e.getMessage() != null ? e.getMessage() : "修改失败");
        }
    }

    /**
     * 绑定手机
     */
    @PostMapping("/bindPhone")
    public Result bindPhone(@RequestBody java.util.Map<String, String> params, HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            return Result.fail("未登录");
        }

        try {
            Claims claims = JwtUtil.parseJwt(token);
            Long userId = Long.parseLong(claims.getId());

            String phone = params.get("phone");
            String code = params.get("code");

            if (phone == null || code == null) {
                return Result.fail("手机号和验证码不能为空");
            }

            userService.bindPhone(userId, phone, code);
            return Result.success("手机绑定成功");
        } catch (Exception e) {
            log.error("绑定手机失败", e);
            return Result.fail(e.getMessage() != null ? e.getMessage() : "绑定失败");
        }
    }

    /**
     * 绑定邮箱
     */
    @PostMapping("/bindEmail")
    public Result bindEmail(@RequestBody java.util.Map<String, String> params, HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            return Result.fail("未登录");
        }

        try {
            Claims claims = JwtUtil.parseJwt(token);
            Long userId = Long.parseLong(claims.getId());

            String email = params.get("email");
            String code = params.get("code");

            if (email == null || code == null) {
                return Result.fail("邮箱和验证码不能为空");
            }

            userService.bindEmail(userId, email, code);
            return Result.success("邮箱绑定成功");
        } catch (Exception e) {
            log.error("绑定邮箱失败", e);
            return Result.fail(e.getMessage() != null ? e.getMessage() : "绑定失败");
        }
    }

    /**
     * 注销账号
     */
    @PostMapping("/delete")
    public Result deleteAccount(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            return Result.fail("未登录");
        }

        try {
            Claims claims = JwtUtil.parseJwt(token);
            Long userId = Long.parseLong(claims.getId());

            userService.deleteAccount(userId);
            return Result.success("账号已注销");
        } catch (Exception e) {
            log.error("注销账号失败", e);
            return Result.fail("注销失败");
        }
    }
}