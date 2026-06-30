package com.ecommerce.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.result.Result;
import com.ecommerce.mall.service.CouponService;
import com.ecommerce.mall.vo.CouponVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/available")
    public Result<Page<CouponVO>> getAvailableCoupons(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(couponService.getAvailableCoupons(userId, page, size));
    }

    @PostMapping("/receive/{couponId}")
    public Result<Void> receiveCoupon(@PathVariable Long couponId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        couponService.receiveCoupon(userId, couponId);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<Page<CouponVO>> getMyCoupons(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(couponService.getMyCoupons(userId, status, page, size));
    }

    @GetMapping("/usable")
    public Result<List<CouponVO>> getUsableCoupons(@RequestParam String orderAmount, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(couponService.getUsableCoupons(userId, new java.math.BigDecimal(orderAmount)));
    }
}