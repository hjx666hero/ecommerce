package com.ecommerce.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.admin.entity.AdminUser;
import com.ecommerce.admin.mapper.AdminUserMapper;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.result.ResultCode;
import com.ecommerce.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public Map<String, Object> login(String username, String password) {
        AdminUser admin = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, username));
        if (admin == null) {
            throw new BusinessException(ResultCode.ADMIN_NOT_EXIST);
        }
        if (admin.getStatus() == 0) {
            throw new BusinessException(ResultCode.ADMIN_DISABLED);
        }
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new BusinessException(ResultCode.ADMIN_PASSWORD_ERROR);
        }
        admin.setLastLoginTime(LocalDateTime.now());
        adminUserMapper.updateById(admin);

        Map<String, Object> result = new HashMap<>();
        result.put("token", jwtUtil.generateAdminToken(admin.getId(), admin.getUsername()));
        result.put("adminId", admin.getId());
        result.put("nickname", admin.getNickname());
        result.put("role", admin.getRole());
        return result;
    }
}