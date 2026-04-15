package com.cjc.controller;

import com.cjc.dto.AdminRegisterDto;
import com.cjc.pojo.TbAdmin;
import com.cjc.service.AdminService;
import com.cjc.util.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "运营商管理",description = "运营商管理接口")
@RequestMapping("/admin")
@RestController
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 根据用户名查询
     * @param username
     * @return
     */
    @GetMapping("/queryByUsername")
    public TbAdmin queryByUsername(@RequestParam String username) {
        return adminService.queryByUsername(username);
    }

    /**
     * 管理员注册
     * @param registerDto
     * @return
     */
    @PostMapping("/register")
    public Result register(@RequestBody AdminRegisterDto registerDto) {
        // 参数校验
        if (StringUtils.isEmpty(registerDto.getUsername()) || StringUtils.isEmpty(registerDto.getPassword())) {
            return Result.fail("用户名和密码不能为空");
        }

        // 检查用户名是否已存在
        TbAdmin existAdmin = adminService.queryByUsername(registerDto.getUsername());
        if (existAdmin != null) {
            return Result.fail("用户名已存在");
        }

        // 创建管理员
        TbAdmin admin = new TbAdmin();
        admin.setUsername(registerDto.getUsername());

        // 生成 salt
        String salt = UUID.randomUUID().toString().replace("-", "");
        admin.setSalt(salt);

        // 使用 Shiro 加密密码 (MD5 + salt + 7次迭代)
        String password = new SimpleHash(
                "md5",
                registerDto.getPassword(),
                ByteSource.Util.bytes(salt),
                7
        ).toHex();
        admin.setPasssword(password);

        // 保存
        adminService.save(admin);

        return Result.success("注册成功");
    }
}
