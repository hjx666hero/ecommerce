package com.ecommerce.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.mall.vo.CouponVO;

import java.util.List;

public interface CouponService {
    Page<CouponVO> getAvailableCoupons(Long userId, Integer page, Integer size);
    void receiveCoupon(Long userId, Long couponId);
    Page<CouponVO> getMyCoupons(Long userId, Integer status, Integer page, Integer size);
    List<CouponVO> getUsableCoupons(Long userId, java.math.BigDecimal orderAmount);
}