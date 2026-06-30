package com.ecommerce.admin.controller;

import com.ecommerce.admin.service.AdminAuthService;
import com.ecommerce.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        return Result.success(adminAuthService.login(params.get("username"), params.get("password")));
    }
}