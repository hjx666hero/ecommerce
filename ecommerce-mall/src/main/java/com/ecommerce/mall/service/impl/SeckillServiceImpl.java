package com.ecommerce.mall.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.constant.OrderStatus;
import com.ecommerce.common.constant.RedisKey;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.result.ResultCode;
import com.ecommerce.common.util.RedisUtil;
import com.ecommerce.mall.dto.SeckillOrderDTO;
import com.ecommerce.mall.entity.*;
import com.ecommerce.mall.mapper.*;
import com.ecommerce.mall.service.SeckillService;
import com.ecommerce.mall.vo.SeckillVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private static final String SECKILL_RATE_LIMIT_KEY = "seckill:rate_limit:";
    private static final int SECKILL_RATE_LIMIT_PER_SECOND = 50; // 每秒最大请求数

    private final SeckillActivityMapper seckillActivityMapper;
    private final SpuMapper spuMapper;
    private final SkuMapper skuMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final AddressMapper addressMapper;
    private final MessageMapper messageMapper;
    private final RedisUtil redisUtil;

    @Override
    public Page<SeckillVO> getSeckillList(Integer page, Integer size) {
        Page<SeckillActivity> activityPage = seckillActivityMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SeckillActivity>()
                        .eq(SeckillActivity::getStatus, 1)
                        .orderByDesc(SeckillActivity::getStartTime));
        List<SeckillVO> voList = activityPage.getRecords().stream()
                .map(this::toSeckillVO)
                .collect(Collectors.toList());
        Page<SeckillVO> result = new Page<>(activityPage.getCurrent(), activityPage.getSize(), activityPage.getTotal());
        result.setRecords(voList);
        return result;
    }

    @Override
    public SeckillVO getSeckillDetail(Long activityId) {
        // 优先从Redis缓存获取
        String cacheKey = "seckill:detail:" + activityId;
        SeckillVO cached = redisUtil.get(cacheKey, SeckillVO.class);
        if (cached != null) {
            return cached;
        }

        SeckillActivity activity = seckillActivityMapper.selectById(activityId);
        if (activity == null || activity.getStatus() == 0) {
            throw new BusinessException(ResultCode.SECKILL_NOT_EXIST);
        }
        SeckillVO vo = toSeckillVO(activity);

        // 缓存1分钟
        redisUtil.set(cacheKey, vo, 60, TimeUnit.SECONDS);
        return vo;
    }

    @Override
    @Transactional
    public String createSeckillOrder(Long userId, SeckillOrderDTO seckillOrderDTO) {
        // 接口级限流（Redis令牌桶）
        String rateLimitKey = SECKILL_RATE_LIMIT_KEY + userId;
        Long currentCount = redisUtil.increment(rateLimitKey, 1);
        if (currentCount == 1) {
            redisUtil.expire(rateLimitKey, 1, TimeUnit.SECONDS);
        }
        if (currentCount > SECKILL_RATE_LIMIT_PER_SECOND) {
            throw new BusinessException(ResultCode.SECKILL_FREQUENT);
        }

        SeckillActivity activity = seckillActivityMapper.selectById(seckillOrderDTO.getActivityId());
        if (activity == null || activity.getStatus() == 0) {
            throw new BusinessException(ResultCode.SECKILL_NOT_EXIST);
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime())) {
            throw new BusinessException(ResultCode.SECKILL_NOT_START);
        }
        if (now.isAfter(activity.getEndTime())) {
            throw new BusinessException(ResultCode.SECKILL_ENDED);
        }

        // 初始化Redis库存（使用SETNX防止重复初始化）
        String stockKey = RedisKey.SECKILL_STOCK + activity.getId();
        redisUtil.tryLock(stockKey + ":init", "1", 30);
        try {
            if (!redisUtil.hasKey(stockKey)) {
                redisUtil.set(stockKey, activity.getStock());
                log.info("秒杀库存已预热: activityId={}, stock={}", activity.getId(), activity.getStock());
            }
        } finally {
            redisUtil.releaseLock(stockKey + ":init", "1");
        }

        // Lua脚本原子扣减Redis库存 + 用户限购检查
        String limitKey = RedisKey.SECKILL_LIMIT + activity.getId() + ":" + userId;
        Long result = redisUtil.seckillDecreaseStock(stockKey, limitKey, activity.getLimitPerUser());
        if (result == -1) {
            throw new BusinessException(ResultCode.SECKILL_STOCK_EMPTY);
        }
        if (result == -2) {
            throw new BusinessException(ResultCode.SECKILL_LIMIT);
        }

        // 原子扣减MySQL库存
        int affected = skuMapper.decreaseSeckillStock(activity.getSkuId(), 1);
        if (affected == 0) {
            // Redis扣了但MySQL没扣成，补偿Redis
            redisUtil.increment(stockKey, 1);
            redisUtil.decrement(limitKey, 1);
            throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
        }

        return createOrder(userId, activity, seckillOrderDTO.getAddressId());
    }

    /**
     * 预热所有进行中秒杀活动的库存到Redis
     */
    @Override
    public void preheatSeckillStock() {
        List<SeckillActivity> activities = seckillActivityMapper.selectList(new LambdaQueryWrapper<SeckillActivity>()
                .eq(SeckillActivity::getStatus, 1)
                .ge(SeckillActivity::getEndTime, LocalDateTime.now()));

        for (SeckillActivity activity : activities) {
            String stockKey = RedisKey.SECKILL_STOCK + activity.getId();
            if (!redisUtil.hasKey(stockKey)) {
                redisUtil.set(stockKey, activity.getStock());
                log.info("秒杀库存预热: activityId={}, stock={}", activity.getId(), activity.getStock());
            }
        }
        log.info("秒杀库存预热完成，共预热 {} 个活动", activities.size());
    }

    private String createOrder(Long userId, SeckillActivity activity, Long addressId) {
        Address address = addressMapper.selectOne(new LambdaQueryWrapper<Address>()
                .eq(Address::getId, addressId).eq(Address::getUserId, userId));
        if (address == null) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_EXIST);
        }

        Sku sku = skuMapper.selectById(activity.getSkuId());
        Spu spu = spuMapper.selectById(activity.getSpuId());

        // 创建订单
        Order order = new Order();
        order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
        order.setUserId(userId);
        order.setTotalAmount(activity.getSeckillPrice());
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(activity.getSeckillPrice());
        order.setStatus(OrderStatus.PENDING_PAY);
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail());
        orderMapper.insert(order);

        // 创建订单商品
        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setOrderNo(order.getOrderNo());
        item.setSpuId(activity.getSpuId());
        item.setSkuId(activity.getSkuId());
        item.setSpuName(spu != null ? spu.getName() : "");
        item.setSkuSpec(sku.getSpecName());
        item.setSkuImage(sku.getImage());
        item.setPrice(activity.getSeckillPrice());
        item.setQuantity(1);
        item.setTotalAmount(activity.getSeckillPrice());
        orderItemMapper.insert(item);

        // 发送消息
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle("秒杀订单创建成功");
        message.setContent("恭喜您秒杀 " + activity.getName() + " 成功，请及时付款。");
        message.setType(3);
        message.setIsRead(0);
        messageMapper.insert(message);

        return order.getOrderNo();
    }

    private SeckillVO toSeckillVO(SeckillActivity activity) {
        SeckillVO vo = new SeckillVO();
        vo.setId(activity.getId());
        vo.setName(activity.getName());
        vo.setSpuId(activity.getSpuId());
        vo.setSkuId(activity.getSkuId());
        vo.setSeckillPrice(activity.getSeckillPrice());
        vo.setStock(activity.getStock());
        vo.setLimitPerUser(activity.getLimitPerUser());
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        vo.setStatus(activity.getStatus());

        Spu spu = spuMapper.selectById(activity.getSpuId());
        if (spu != null) {
            vo.setSpuName(spu.getName());
            vo.setSpuImage(spu.getMainImage());
        }
        Sku sku = skuMapper.selectById(activity.getSkuId());
        if (sku != null) {
            vo.setSkuSpec(sku.getSpecName());
            vo.setOriginalPrice(sku.getPrice());
        }

        // 计算剩余秒数
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime())) {
            vo.setRemainSeconds(Duration.between(now, activity.getStartTime()).getSeconds());
        } else if (now.isBefore(activity.getEndTime())) {
            vo.setRemainSeconds(Duration.between(now, activity.getEndTime()).getSeconds());
        } else {
            vo.setRemainSeconds(0L);
        }
        return vo;
    }
}