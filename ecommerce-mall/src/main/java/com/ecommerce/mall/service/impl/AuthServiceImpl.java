package com.ecommerce.mall.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.common.constant.RedisKey;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.result.ResultCode;
import com.ecommerce.common.util.JwtUtil;
import com.ecommerce.common.util.RedisUtil;
import com.ecommerce.mall.dto.LoginDTO;
import com.ecommerce.mall.dto.RegisterDTO;
import com.ecommerce.mall.entity.User;
import com.ecommerce.mall.mapper.UserMapper;
import com.ecommerce.mall.service.AuthService;
import com.ecommerce.mall.vo.LoginVO;
import com.ecommerce.mall.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    @Override
    public void sendVerifyCode(String phone) {
        String code = RandomUtil.randomNumbers(6);
        String key = RedisKey.VERIFY_CODE + phone;
        redisUtil.set(key, code, RedisKey.VERIFY_CODE_EXPIRE);
        // 实际项目中应调用短信服务发送验证码
        System.out.println("验证码发送至 " + phone + ": " + code);
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .and(w -> w.eq(User::getPhone, loginDTO.getAccount())
                        .or().eq(User::getEmail, loginDTO.getAccount())
                        .or().eq(User::getUsername, loginDTO.getAccount())));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_ACCOUNT_DISABLED);
        }
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        return buildLoginVO(user);
    }


    @Override
    public LoginVO loginBySms(String phone, String code) {
        // 1. 校验验证码
        String key = RedisKey.VERIFY_CODE + phone;
        String storedCode = redisUtil.get(key, String.class);
        if (storedCode == null) {
            throw new BusinessException(ResultCode.VERIFY_CODE_EXPIRED);
        }
        if (!storedCode.equals(code)) {
            throw new BusinessException(ResultCode.VERIFY_CODE_ERROR);
        }

        // 2. 根据手机号查询用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_ACCOUNT_DISABLED);
        }

        // 3. 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 4. 删除已使用的验证码（防止重复使用）
        redisUtil.delete(key);

        // 5. 生成Token并返回
        return buildLoginVO(user);
    }

    @Override
    @Transactional
    public LoginVO register(RegisterDTO registerDTO) {
        // 验证验证码
        String key = RedisKey.VERIFY_CODE + registerDTO.getPhone();
        String storedCode = redisUtil.get(key, String.class);
        if (storedCode == null) {
            throw new BusinessException(ResultCode.VERIFY_CODE_EXPIRED);
        }
        if (!storedCode.equals(registerDTO.getVerifyCode())) {
            throw new BusinessException(ResultCode.VERIFY_CODE_ERROR);
        }
        // 检查手机号是否已注册
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, registerDTO.getPhone())) > 0) {
            throw new BusinessException(ResultCode.USER_PHONE_EXIST);
        }
        // 创建用户
        User user = new User();
        user.setPhone(registerDTO.getPhone());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : "用户" + RandomUtil.randomNumbers(6));
        user.setEmail(registerDTO.getEmail());
        user.setStatus(1);
        userMapper.insert(user);
        // 删除验证码
        redisUtil.delete(key);

        return buildLoginVO(user);
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
        try {
            String accessToken = jwtUtil.refreshAccessToken(refreshToken);
            LoginVO vo = new LoginVO();
            vo.setAccessToken(accessToken);
            vo.setRefreshToken(refreshToken);
            return vo;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.REFRESH_TOKEN_EXPIRED);
        }
    }

    @Override
    @Transactional
    public void resetPassword(String phone, String verifyCode, String newPassword) {
        String key = RedisKey.VERIFY_CODE + phone;
        String storedCode = redisUtil.get(key, String.class);
        if (storedCode == null) {
            throw new BusinessException(ResultCode.VERIFY_CODE_EXPIRED);
        }
        if (!storedCode.equals(verifyCode)) {
            throw new BusinessException(ResultCode.VERIFY_CODE_ERROR);
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        redisUtil.delete(key);
    }

    private LoginVO buildLoginVO(User user) {
        LoginVO vo = new LoginVO();
        vo.setAccessToken(jwtUtil.generateAccessToken(user.getId(), user.getPhone()));
        vo.setRefreshToken(jwtUtil.generateRefreshToken(user.getId(), user.getPhone()));
        vo.setUserId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        return vo;
    }
}