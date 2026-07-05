package com.ecommerce.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.mall.entity.Coupon;
import com.ecommerce.mall.entity.UserCoupon;
import com.ecommerce.mall.mapper.CouponMapper;
import com.ecommerce.mall.mapper.UserCouponMapper;
import com.ecommerce.mall.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final UserMapper userMapper;

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

    /**
     * 批量发放优惠券
     * @param couponId 优惠券ID
     * @param userIds 用户ID列表，null或空则全量发放
     */
    @Transactional
    public int batchDistribute(Long couponId, List<Long> userIds) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || coupon.getStatus() == 0) {
            throw new RuntimeException("优惠券不存在或已失效");
        }

        // 如果未指定用户，全量发放给所有用户
        if (userIds == null || userIds.isEmpty()) {
            userIds = userMapper.selectList(null).stream()
                    .map(u -> u.getId())
                    .toList();
        }

        int successCount = 0;
        int remainStock = coupon.getTotalCount() - coupon.getReceiveCount();

        for (Long userId : userIds) {
            if (remainStock <= 0) break;

            // 检查是否已领取
            long count = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                    .eq(UserCoupon::getUserId, userId)
                    .eq(UserCoupon::getCouponId, couponId));
            if (count >= coupon.getPerLimit()) {
                continue;
            }

            // CAS扣减库存
            int affected = couponMapper.incrReceiveCount(couponId);
            if (affected == 0) break;

            UserCoupon uc = new UserCoupon();
            uc.setUserId(userId);
            uc.setCouponId(couponId);
            uc.setStatus(0);
            userCouponMapper.insert(uc);
            successCount++;
            remainStock--;
        }

        log.info("批量发券完成: couponId={}, 成功发放 {} 张", couponId, successCount);
        return successCount;
    }
}