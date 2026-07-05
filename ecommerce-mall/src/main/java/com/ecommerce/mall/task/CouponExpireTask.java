package com.ecommerce.mall.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ecommerce.mall.entity.Coupon;
import com.ecommerce.mall.entity.UserCoupon;
import com.ecommerce.mall.mapper.CouponMapper;
import com.ecommerce.mall.mapper.UserCouponMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券过期自动标记定时任务
 * 每5分钟扫描一次，将已过期但未使用的优惠券标记为已过期
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponExpireTask {

    private final UserCouponMapper userCouponMapper;
    private final CouponMapper couponMapper;

    @Scheduled(fixedDelay = 300000) // 每5分钟执行一次
    @Transactional
    public void markExpiredCoupons() {
        // 找出所有已过期的优惠券模板
        List<Coupon> expiredTemplates = couponMapper.selectList(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getStatus, 1)
                .lt(Coupon::getEndTime, LocalDateTime.now()));
        
        if (expiredTemplates.isEmpty()) {
            return;
        }

        // 标记这些优惠券模板为已失效
        for (Coupon coupon : expiredTemplates) {
            coupon.setStatus(0);
            couponMapper.updateById(coupon);
        }

        // 标记用户已领取但未使用的过期的优惠券
        for (Coupon coupon : expiredTemplates) {
            int affected = userCouponMapper.update(null, new LambdaUpdateWrapper<UserCoupon>()
                    .eq(UserCoupon::getCouponId, coupon.getId())
                    .eq(UserCoupon::getStatus, 0)
                    .set(UserCoupon::getStatus, 2));
            if (affected > 0) {
                log.info("已标记 {} 张过期优惠券 (模板: {})", affected, coupon.getName());
            }
        }

        log.info("优惠券过期标记完成，共处理 {} 个过期模板", expiredTemplates.size());
    }
}
