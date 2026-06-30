package com.ecommerce.mall.controller;

import com.ecommerce.common.result.Result;
import com.ecommerce.mall.service.UserService;
import com.ecommerce.mall.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public Result<UserVO> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updateProfile(userId,
                (String) params.get("nickname"),
                (String) params.get("avatar"),
                params.get("gender") != null ? ((Number) params.get("gender")).intValue() : null);
        return Result.success();
    }

    @PutMapping("/change-password")
    public Result<Void> changePassword(HttpServletRequest request, @RequestBody Map<String, String> params) {
        Long userId = (Long) request.getAttribute("userId");
        userService.changePassword(userId, params.get("oldPassword"), params.get("newPassword"));
        return Result.success();
    }
}