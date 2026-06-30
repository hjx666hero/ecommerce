package com.ecommerce.mall.controller;

import com.ecommerce.common.result.Result;
import com.ecommerce.mall.dto.LoginDTO;
import com.ecommerce.mall.dto.RegisterDTO;
import com.ecommerce.mall.service.AuthService;
import com.ecommerce.mall.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-code")
    public Result<Void> sendVerifyCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        authService.sendVerifyCode(phone);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return Result.success(authService.register(registerDTO));
    }

    @PostMapping("/refresh-token")
    public Result<LoginVO> refreshToken(@RequestBody Map<String, String> params) {
        return Result.success(authService.refreshToken(params.get("refreshToken")));
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, String> params) {
        authService.resetPassword(params.get("phone"), params.get("verifyCode"), params.get("newPassword"));
        return Result.success();
    }
}