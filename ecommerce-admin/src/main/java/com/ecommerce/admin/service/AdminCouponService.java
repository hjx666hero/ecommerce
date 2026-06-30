package com.ecommerce.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.mall.entity.Coupon;
import com.ecommerce.mall.mapper.CouponMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminCouponService {

    private final CouponMapper couponMapper;

    public Page<Coupon> getCouponList(Integer page, Integer size) {
        return couponMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Coupon>().orderByDesc(Coupon::getCreateTime));
    }

    @Transactional
    public void saveCoupon(Coupon coupon) {
        if (coupon.getId() == null) {
            couponMapper.insert(coupon);
        } else {
            couponMapper.updateById(coupon);
        }
    }

    @Transactional
    public void deleteCoupon(Long id) {
        couponMapper.deleteById(id);
    }
}