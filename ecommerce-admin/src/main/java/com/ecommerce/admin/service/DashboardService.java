package com.ecommerce.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.mall.mapper.OrderMapper;
import com.ecommerce.mall.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserMapper userMapper;
    private final OrderMapper orderMapper;

    public Map<String, Object> getDashboard() {
        Map<String, Object> result = new HashMap<>();

        // 总用户数
        result.put("totalUsers", userMapper.selectCount(null));

        // 总订单数
        result.put("totalOrders", orderMapper.selectCount(null));

        // 今日销售额
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        BigDecimal todaySales = orderMapper.selectList(new LambdaQueryWrapper<com.ecommerce.mall.entity.Order>()
                .ge(com.ecommerce.mall.entity.Order::getCreateTime, todayStart)
                .le(com.ecommerce.mall.entity.Order::getCreateTime, todayEnd)
                .in(com.ecommerce.mall.entity.Order::getStatus, 1, 2, 3))
                .stream()
                .map(o -> o.getPayAmount() != null ? o.getPayAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("todaySales", todaySales);

        // 今日订单数
        result.put("todayOrders", orderMapper.selectCount(new LambdaQueryWrapper<com.ecommerce.mall.entity.Order>()
                .ge(com.ecommerce.mall.entity.Order::getCreateTime, todayStart)
                .le(com.ecommerce.mall.entity.Order::getCreateTime, todayEnd)));

        return result;
    }
}