package com.cjc.service.impl;

import com.cjc.dto.UserRegisterDto;
import com.cjc.dto.UserUpdateDto;
import com.cjc.exception.BusinessException;
import com.cjc.mapper.TbUserMapper;
import com.cjc.pojo.TbUser;
import com.cjc.pojo.TbUserExample;
import com.cjc.service.UserService;
import com.cjc.util.Result;
import com.cjc.util.SmsUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private TbUserMapper userMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private SmsUtil smsUtil;
    //redis key前缀
    private static final String SMS_CODE_PREFIX = "sms:code:";
    //验证码有效期
    private static final int CODE_EXPIRE_MINUTES = 5;
    @Override
    public TbUser queryByUsername(String username) {
        TbUserExample example = new TbUserExample();
        example.createCriteria().andUsernameEqualTo(username);
        List<TbUser> users = userMapper.selectByExample(example);
        return users != null && !users.isEmpty() ? users.get(0) : null;
    }

    @Override
    public TbUser queryById(Long userId) {
        return userMapper.selectByPrimaryKey(userId);
    }

    @Override
    public void register(UserRegisterDto userdto) {
        // 1. 检查用户名是否存在
        if (queryByUsername(userdto.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        // 2. 验证码校验
        String code = userdto.getCode();
        if (code == null || code.trim().isEmpty()) {
            throw new BusinessException("请输入验证码");
        }
        String savedCode = (String) redisTemplate.opsForValue().get(SMS_CODE_PREFIX + userdto.getPhone());
        if (savedCode == null) {
            throw new BusinessException("验证码已过期，请重新获取");
        }
        if (!code.equals(savedCode)) {
            throw new BusinessException("验证码错误");
        }
        // 3. 验证通过，删除验证码
        redisTemplate.delete(SMS_CODE_PREFIX + userdto.getPhone());
        TbUser tbUser = new TbUser();
        // 生成盐值
        String salt = UUID.randomUUID().toString().replace("-", "");
        tbUser.setSalt(salt);
        tbUser.setPassword(userdto.getPassword());
        tbUser.setPhone(userdto.getPhone());
        tbUser.setUsername(userdto.getUsername());
        // 加密密码
        String encryptedPassword = new SimpleHash(
            "MD5",
                tbUser.getPassword(),
            ByteSource.Util.bytes(salt),
            7
        ).toHex();
        tbUser.setPassword(encryptedPassword);
        // 设置默认值
        tbUser.setStatus("Y");
        tbUser.setCreated(new Date());
        tbUser.setUpdated(new Date());
        tbUser.setIsMobileCheck("0");
        tbUser.setIsEmailCheck("0");
        tbUser.setPoints(0);
        tbUser.setExperienceValue(0);
        tbUser.setAccountBalance(0L);
        
        userMapper.insertSelective(tbUser);
    }

    @Override
    public void sendRegisterCode(String phone) {
        // 1. 检查手机号是否已注册
        TbUserExample phoneExample = new TbUserExample();
        phoneExample.createCriteria().andPhoneEqualTo(phone);
        if (userMapper.countByExample(phoneExample) > 0) {
            throw new BusinessException("手机号已被注册");
        }

        String key = SMS_CODE_PREFIX + phone;

        // 2. 检查是否已有未过期的验证码
        String existingCode = (String) redisTemplate.opsForValue().get(key);
        String code;

        if (existingCode != null) {
            // 验证码未过期，复用原验证码重新发送
            code = existingCode;
            log.info("复用已有验证码，手机号: {}", phone);
            Result<String> result = smsUtil.sendSms(phone, code);
            if (!"10000".equals(result.getCode())) {
                throw new BusinessException(result.getMessage());
            }
        } else {
            // 验证码已过期或不存在，生成新的验证码
            Result<String> result = smsUtil.sendVerifyCode(phone);
            if (!"10000".equals(result.getCode())) {
                throw new BusinessException(result.getMessage());
            }
            code = result.getData();
            redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
    }

    @Override
    public void updateUser(UserUpdateDto userDto) {
        // 1. 检查用户是否存在
        TbUser existUser = userMapper.selectByPrimaryKey(userDto.getId());
        if (existUser == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 2. 复制属性
        TbUser updateUser = new TbUser();
        updateUser.setId(userDto.getId());
        updateUser.setNickName(userDto.getNickName());
        updateUser.setName(userDto.getName());
        updateUser.setHeadPic(userDto.getHeadPic());
        updateUser.setSex(userDto.getSex());
        updateUser.setBirthday(userDto.getBirthday());
        updateUser.setQq(userDto.getQq());
        updateUser.setEmail(userDto.getEmail());
        updateUser.setUpdated(new Date());
        
        // 3. 更新
        userMapper.updateByPrimaryKeySelective(updateUser);
    }

    @Override
    public void sendSmsCode(String phone) {
        // 检查发送频率（1分钟内只能发一次）
        String key = SMS_CODE_PREFIX + phone;
        String existingCode = (String) redisTemplate.opsForValue().get(key);
        if (existingCode != null) {
            // 验证码未过期，复用原验证码
            log.info("复用已有验证码，手机号: {}", phone);
        } else {
            // 生成新验证码
            Result<String> result = smsUtil.sendVerifyCode(phone);
            if (!"10000".equals(result.getCode())) {
                throw new BusinessException(result.getMessage());
            }
            redisTemplate.opsForValue().set(key, result.getData(), CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
    }

    @Override
    public void sendEmailCode(String email) {
        // 生成6位验证码
        String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
        String key = "email:code:" + email;
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 这里需要邮件发送服务，暂时只存储验证码，实际发送需配置邮件服务
        log.info("邮箱验证码已生成: email={}, code={}", email, code);
        // 实际项目中应调用邮件发送服务
        // mailUtil.sendMail(email, "验证码", "您的验证码是：" + code);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        TbUser user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证旧密码
        String encryptedOldPassword = new SimpleHash(
            "MD5",
            oldPassword,
            ByteSource.Util.bytes(user.getSalt()),
            7
        ).toHex();

        if (!encryptedOldPassword.equals(user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 加密新密码
        String encryptedNewPassword = new SimpleHash(
            "MD5",
            newPassword,
            ByteSource.Util.bytes(user.getSalt()),
            7
        ).toHex();

        // 更新密码
        TbUser updateUser = new TbUser();
        updateUser.setId(userId);
        updateUser.setPassword(encryptedNewPassword);
        updateUser.setUpdated(new Date());
        userMapper.updateByPrimaryKeySelective(updateUser);
    }

    @Override
    public void bindPhone(Long userId, String phone, String code) {
        // 验证验证码
        String key = SMS_CODE_PREFIX + phone;
        String savedCode = (String) redisTemplate.opsForValue().get(key);
        if (savedCode == null) {
            throw new BusinessException("验证码已过期");
        }
        if (!code.equals(savedCode)) {
            throw new BusinessException("验证码错误");
        }

        // 检查手机号是否已被其他用户绑定
        TbUserExample example = new TbUserExample();
        example.createCriteria().andPhoneEqualTo(phone).andIdNotEqualTo(userId);
        if (userMapper.countByExample(example) > 0) {
            throw new BusinessException("手机号已被其他用户绑定");
        }

        // 更新手机号
        TbUser updateUser = new TbUser();
        updateUser.setId(userId);
        updateUser.setPhone(phone);
        updateUser.setIsMobileCheck("1");
        updateUser.setUpdated(new Date());
        userMapper.updateByPrimaryKeySelective(updateUser);

        // 删除验证码
        redisTemplate.delete(key);
    }

    @Override
    public void bindEmail(Long userId, String email, String code) {
        // 验证验证码
        String key = "email:code:" + email;
        String savedCode = (String) redisTemplate.opsForValue().get(key);
        if (savedCode == null) {
            throw new BusinessException("验证码已过期");
        }
        if (!code.equals(savedCode)) {
            throw new BusinessException("验证码错误");
        }

        // 检查邮箱是否已被其他用户绑定
        TbUserExample example = new TbUserExample();
        example.createCriteria().andEmailEqualTo(email).andIdNotEqualTo(userId);
        if (userMapper.countByExample(example) > 0) {
            throw new BusinessException("邮箱已被其他用户绑定");
        }

        // 更新邮箱
        TbUser updateUser = new TbUser();
        updateUser.setId(userId);
        updateUser.setEmail(email);
        updateUser.setIsEmailCheck("1");
        updateUser.setUpdated(new Date());
        userMapper.updateByPrimaryKeySelective(updateUser);

        // 删除验证码
        redisTemplate.delete(key);
    }

    @Override
    public void deleteAccount(Long userId) {
        TbUser user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 实际项目中应该：
        // 1. 检查用户是否有未完成的订单
        // 2. 处理用户相关数据（订单、地址、购物车等）
        // 3. 软删除或标记为已注销

        // 这里简单处理：标记状态为已注销
        TbUser updateUser = new TbUser();
        updateUser.setId(userId);
        updateUser.setStatus("N");  // 标记为无效
        updateUser.setUpdated(new Date());
        userMapper.updateByPrimaryKeySelective(updateUser);

        log.info("用户账号已注销: userId={}", userId);
    }
}