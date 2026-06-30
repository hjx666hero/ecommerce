package com.ecommerce.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.admin.service.AdminUserService;
import com.ecommerce.common.result.Result;
import com.ecommerce.mall.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class UserController {

    private final AdminUserService adminUserService;

    @GetMapping("/list")
    public Result<Page<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return Result.success(adminUserService.getUserList(page, size, keyword));
    }

    @GetMapping("/{userId}")
    public Result<User> getUserDetail(@PathVariable Long userId) {
        return Result.success(adminUserService.getUserDetail(userId));
    }

    @PutMapping("/{userId}/status")
    public Result<Void> updateStatus(@PathVariable Long userId, @RequestBody Map<String, Integer> params) {
        adminUserService.updateStatus(userId, params.get("status"));
        return Result.success();
    }
}