package com.ecommerce.mall.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ecommerce.common.constant.OrderStatus;
import com.ecommerce.mall.entity.Order;
import com.ecommerce.mall.entity.OrderItem;
import com.ecommerce.mall.mapper.OrderItemMapper;
import com.ecommerce.mall.mapper.OrderMapper;
import com.ecommerce.mall.mapper.SkuMapper;
import com.ecommerce.mall.mapper.UserCouponMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时自动取消定时任务
 * 每30秒扫描一次，将超过15分钟未支付的订单自动取消
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final SkuMapper skuMapper;
    private final UserCouponMapper userCouponMapper;

    private static final int TIMEOUT_MINUTES = 15;

    @Scheduled(fixedDelay = 30000) // 每30秒执行一次
    @Transactional
    public void cancelTimeoutOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES);
        
        // 查询超时未支付的订单
        List<Order> timeoutOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, OrderStatus.PENDING_PAY)
                .lt(Order::getCreateTime, deadline)
                .last("LIMIT 50")); // 每次最多处理50单

        if (timeoutOrders.isEmpty()) {
            return;
        }

        log.info("发现 {} 个超时未支付订单，开始自动取消", timeoutOrders.size());

        for (Order order : timeoutOrders) {
            try {
                cancelOrderInternal(order);
                log.info("订单 {} 已自动取消（超时{}分钟未支付）", order.getOrderNo(), TIMEOUT_MINUTES);
            } catch (Exception e) {
                log.error("自动取消订单 {} 失败: {}", order.getOrderNo(), e.getMessage());
            }
        }
    }

    private void cancelOrderInternal(Order order) {
        // 更新订单状态
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // CAS原子回滚库存
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId()));
        for (OrderItem item : items) {
            skuMapper.rollbackStock(item.getSkuId(), item.getQuantity());
        }

        // 优惠券返还
        if (order.getCouponId() != null) {
            userCouponMapper.update(null, new LambdaUpdateWrapper<com.ecommerce.mall.entity.UserCoupon>()
                    .eq(com.ecommerce.mall.entity.UserCoupon::getUserId, order.getUserId())
                    .eq(com.ecommerce.mall.entity.UserCoupon::getCouponId, order.getCouponId())
                    .eq(com.ecommerce.mall.entity.UserCoupon::getStatus, 1)
                    .set(com.ecommerce.mall.entity.UserCoupon::getStatus, 0)
                    .set(com.ecommerce.mall.entity.UserCoupon::getUseTime, null)
                    .set(com.ecommerce.mall.entity.UserCoupon::getOrderNo, null));
        }
    }
}
