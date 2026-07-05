package com.ecommerce.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.mall.dto.OrderCreateDTO;
import com.ecommerce.mall.vo.OrderVO;

public interface OrderService {
    OrderVO createOrder(Long userId, OrderCreateDTO orderCreateDTO);
    Page<OrderVO> getOrderList(Long userId, Integer status, Integer page, Integer size);
    OrderVO getOrderDetail(Long userId, Long orderId);
    OrderVO getOrderDetailByOrderNo(Long userId, String orderNo);
    void cancelOrder(Long userId, Long orderId);
    void cancelOrderByOrderNo(Long userId, String orderNo);
    void confirmReceive(Long userId, Long orderId);
    void confirmReceiveByOrderNo(Long userId, String orderNo);
    void applyRefund(Long userId, Long orderId);
    void applyRefundByOrderNo(Long userId, String orderNo);
    void deleteOrder(Long userId, Long orderId);
}