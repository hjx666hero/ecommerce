package com.ecommerce.mall.service;

import com.ecommerce.mall.dto.LoginDTO;
import com.ecommerce.mall.dto.RegisterDTO;
import com.ecommerce.mall.vo.LoginVO;
import com.ecommerce.mall.vo.UserVO;

public interface AuthService {
    void sendVerifyCode(String phone);
    LoginVO login(LoginDTO loginDTO);
    LoginVO loginBySms(String phone, String code);
    LoginVO register(RegisterDTO registerDTO);
    LoginVO refreshToken(String refreshToken);
    void resetPassword(String phone, String verifyCode, String newPassword);
}