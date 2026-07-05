package com.ecommerce.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.constant.RedisKey;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.result.ResultCode;
import com.ecommerce.common.util.RedisUtil;
import com.ecommerce.mall.entity.Coupon;
import com.ecommerce.mall.entity.UserCoupon;
import com.ecommerce.mall.mapper.CouponMapper;
import com.ecommerce.mall.mapper.UserCouponMapper;
import com.ecommerce.mall.service.CouponService;
import com.ecommerce.mall.vo.CouponVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final RedisUtil redisUtil;

    @Override
    public Page<CouponVO> getAvailableCoupons(Long userId, Integer page, Integer size) {
        Page<Coupon> couponPage = couponMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Coupon>()
                        .eq(Coupon::getStatus, 1)
                        .lt(Coupon::getStartTime, LocalDateTime.now())
                        .gt(Coupon::getEndTime, LocalDateTime.now())
                        .orderByDesc(Coupon::getCreateTime));

        // 获取用户已领取的优惠券
        List<UserCoupon> userCoupons = userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId));
        Set<Long> receivedIds = userCoupons.stream().map(UserCoupon::getCouponId).collect(Collectors.toSet());

        List<CouponVO> voList = couponPage.getRecords().stream().map(coupon -> {
            CouponVO vo = toCouponVO(coupon);
            vo.setReceived(receivedIds.contains(coupon.getId()));
            return vo;
        }).collect(Collectors.toList());

        Page<CouponVO> result = new Page<>(couponPage.getCurrent(), couponPage.getSize(), couponPage.getTotal());
        result.setRecords(voList);
        return result;
    }

    @Override
    @Transactional
    public void receiveCoupon(Long userId, Long couponId) {
        String lockKey = RedisKey.LOCK_COUPON + couponId + ":" + userId;
        String lockValue = UUID.randomUUID().toString();
        if (!redisUtil.tryLock(lockKey, lockValue, 5)) {
            throw new BusinessException(ResultCode.COUPON_RECEIVED);
        }
        try {
            Coupon coupon = couponMapper.selectById(couponId);
            if (coupon == null || coupon.getStatus() == 0) {
                throw new BusinessException(ResultCode.COUPON_NOT_EXIST);
            }
            if (LocalDateTime.now().isBefore(coupon.getStartTime()) || LocalDateTime.now().isAfter(coupon.getEndTime())) {
                throw new BusinessException(ResultCode.COUPON_EXPIRED);
            }
            if (coupon.getReceiveCount() >= coupon.getTotalCount()) {
                throw new BusinessException(ResultCode.COUPON_EXHAUSTED);
            }
            // 检查是否已领取
            Long count = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                    .eq(UserCoupon::getUserId, userId).eq(UserCoupon::getCouponId, couponId));
            if (count >= coupon.getPerLimit()) {
                throw new BusinessException(ResultCode.COUPON_RECEIVED);
            }
            // CAS原子扣减库存
            int affected = couponMapper.incrReceiveCount(couponId);
            if (affected == 0) {
                throw new BusinessException(ResultCode.COUPON_EXHAUSTED);
            }
            // 发放
            UserCoupon uc = new UserCoupon();
            uc.setUserId(userId);
            uc.setCouponId(couponId);
            uc.setStatus(0);
            userCouponMapper.insert(uc);
        } finally {
            redisUtil.releaseLock(lockKey, lockValue);
        }
    }

    @Override
    public Page<CouponVO> getMyCoupons(Long userId, Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .orderByDesc(UserCoupon::getCreateTime);
        if (status != null) {
            wrapper.eq(UserCoupon::getStatus, status);
        }
        Page<UserCoupon> ucPage = userCouponMapper.selectPage(new Page<>(page, size), wrapper);
        List<Long> couponIds = ucPage.getRecords().stream().map(UserCoupon::getCouponId).collect(Collectors.toList());
        if (couponIds.isEmpty()) {
            return new Page<>(page, size, 0);
        }
        List<Coupon> coupons = couponMapper.selectBatchIds(couponIds);
        Map<Long, Coupon> couponMap = coupons.stream().collect(Collectors.toMap(Coupon::getId, c -> c));

        List<CouponVO> voList = ucPage.getRecords().stream().map(uc -> {
            Coupon coupon = couponMap.get(uc.getCouponId());
            if (coupon == null) return null;
            CouponVO vo = toCouponVO(coupon);
            vo.setReceived(true);
            return vo;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        Page<CouponVO> result = new Page<>(ucPage.getCurrent(), ucPage.getSize(), ucPage.getTotal());
        result.setRecords(voList);
        return result;
    }

    @Override
    public List<CouponVO> getUsableCoupons(Long userId, BigDecimal orderAmount) {
        List<UserCoupon> userCoupons = userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId).eq(UserCoupon::getStatus, 0));
        if (userCoupons.isEmpty()) return List.of();

        List<Long> couponIds = userCoupons.stream().map(UserCoupon::getCouponId).collect(Collectors.toList());
        List<Coupon> coupons = couponMapper.selectBatchIds(couponIds);

        return coupons.stream()
                .filter(c -> c.getStatus() == 1
                        && LocalDateTime.now().isAfter(c.getStartTime())
                        && LocalDateTime.now().isBefore(c.getEndTime())
                        && orderAmount.compareTo(c.getMinAmount()) >= 0)
                .map(this::toCouponVO)
                .collect(Collectors.toList());
    }

    private CouponVO toCouponVO(Coupon coupon) {
        CouponVO vo = new CouponVO();
        vo.setId(coupon.getId());
        vo.setName(coupon.getName());
        vo.setType(coupon.getType());
        vo.setTypeText(getTypeText(coupon.getType()));
        vo.setDiscountValue(coupon.getDiscountValue());
        vo.setMinAmount(coupon.getMinAmount());
        vo.setTotalCount(coupon.getTotalCount());
        vo.setReceiveCount(coupon.getReceiveCount());
        vo.setPerLimit(coupon.getPerLimit());
        vo.setStartTime(coupon.getStartTime());
        vo.setEndTime(coupon.getEndTime());
        vo.setStatus(coupon.getStatus());
        return vo;
    }

    private String getTypeText(Integer type) {
        switch (type) {
            case 1: return "满减券";
            case 2: return "折扣券";
            case 3: return "无门槛券";
            default: return "未知";
        }
    }
}