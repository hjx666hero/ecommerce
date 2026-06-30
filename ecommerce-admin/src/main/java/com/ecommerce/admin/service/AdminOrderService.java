package com.ecommerce.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.constant.OrderStatus;
import com.ecommerce.mall.entity.Order;
import com.ecommerce.mall.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
}