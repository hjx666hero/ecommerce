package com.ecommerce.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.admin.service.AdminCouponService;
import com.ecommerce.common.result.Result;
import com.ecommerce.mall.entity.Coupon;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final AdminCouponService adminCouponService;

    @GetMapping("/list")
    public Result<Page<Coupon>> getCouponList(@RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(adminCouponService.getCouponList(page, size));
    }

    @PostMapping
    public Result<Void> saveCoupon(@RequestBody Coupon coupon) {
        adminCouponService.saveCoupon(coupon);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCoupon(@PathVariable Long id) {
        adminCouponService.deleteCoupon(id);
        return Result.success();
    }

    /**
     * 批量发放优惠券
     */
    @PostMapping("/{couponId}/distribute")
    public Result<Integer> batchDistribute(@PathVariable Long couponId, @RequestBody(required = false) Map<String, List<Long>> body) {
        List<Long> userIds = body != null ? body.get("userIds") : null;
        return Result.success(adminCouponService.batchDistribute(couponId, userIds));
    }
}