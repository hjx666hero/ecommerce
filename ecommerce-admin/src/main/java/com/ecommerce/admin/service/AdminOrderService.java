package com.ecommerce.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.constant.OrderStatus;
import com.ecommerce.mall.entity.Order;
import com.ecommerce.mall.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderMapper orderMapper;

    public Order getOrderDetail(Long orderId) {
        return orderMapper.selectById(orderId);
    }

    public Page<Order> getOrderList(Integer page, Integer size, Integer status, String keyword, String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .orderByDesc(Order::getCreateTime);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Order::getReceiverName, keyword)
                    .or().like(Order::getReceiverPhone, keyword));
        }
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.eq(Order::getOrderNo, orderNo);
        }
        return orderMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Transactional
    public void deliverOrder(Long orderId, String logisticsCompany, String logisticsNo) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != OrderStatus.PAID) {
            throw new RuntimeException("订单状态不正确");
        }
        order.setStatus(OrderStatus.DELIVERED);
        order.setLogisticsCompany(logisticsCompany);
        order.setLogisticsNo(logisticsNo);
        order.setDeliveryTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Transactional
    public void handleRefund(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        order.setStatus(OrderStatus.REFUNDED);
        orderMapper.updateById(order);
    }

    /**
     * 订单统计：今日/全部 销售额、订单量、客单价
     */
    public Map<String, Object> getOrderStatistics() {
        // 今日零点
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        // 全部有效订单（排除取消和退款的）
        LambdaQueryWrapper<Order> allValidWrapper = new LambdaQueryWrapper<Order>()
                .notIn(Order::getStatus, OrderStatus.CANCELLED, OrderStatus.REFUNDED, OrderStatus.PENDING_PAY);

        // 今日有效订单
        LambdaQueryWrapper<Order> todayValidWrapper = new LambdaQueryWrapper<Order>()
                .notIn(Order::getStatus, OrderStatus.CANCELLED, OrderStatus.REFUNDED, OrderStatus.PENDING_PAY)
                .ge(Order::getCreateTime, todayStart);

        // 查询统计数据
        Long totalOrders = orderMapper.selectCount(allValidWrapper);
        Long todayOrders = orderMapper.selectCount(todayValidWrapper);

        // 统计销售额
        BigDecimal totalSales = BigDecimal.ZERO;
        BigDecimal todaySales = BigDecimal.ZERO;
        var allOrders = orderMapper.selectList(allValidWrapper);
        var todayOrderList = orderMapper.selectList(todayValidWrapper);
        
        totalSales = allOrders.stream()
                .map(Order::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        todaySales = todayOrderList.stream()
                .map(Order::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgOrderValue = totalOrders > 0 
                ? totalSales.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP) 
                : BigDecimal.ZERO;
        BigDecimal todayAvgOrderValue = todayOrders > 0 
                ? todaySales.divide(BigDecimal.valueOf(todayOrders), 2, BigDecimal.ROUND_HALF_UP) 
                : BigDecimal.ZERO;

        // 各状态订单数
        Map<String, Long> statusCountMap = new LinkedHashMap<>();
        for (int i = 0; i <= 5; i++) {
            Long count = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                    .eq(Order::getStatus, i));
            statusCountMap.put(getStatusName(i), count);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalOrders", totalOrders);
        result.put("totalSales", totalSales);
        result.put("avgOrderValue", avgOrderValue);
        result.put("todayOrders", todayOrders);
        result.put("todaySales", todaySales);
        result.put("todayAvgOrderValue", todayAvgOrderValue);
        result.put("statusCount", statusCountMap);
        return result;
    }

    private String getStatusName(Integer status) {
        switch (status) {
            case 0: return "待付款";
            case 1: return "已支付";
            case 2: return "已发货";
            case 3: return "已完成";
            case 4: return "已取消";
            case 5: return "已退款";
            default: return "未知";
        }
    }
}