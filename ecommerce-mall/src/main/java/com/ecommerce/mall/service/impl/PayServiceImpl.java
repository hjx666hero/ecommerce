package com.ecommerce.mall.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.common.constant.OrderStatus;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.result.ResultCode;
import com.ecommerce.mall.entity.Order;
import com.ecommerce.mall.entity.PayRecord;
import com.ecommerce.mall.mapper.OrderMapper;
import com.ecommerce.mall.mapper.PayRecordMapper;
import com.ecommerce.mall.service.PayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PayServiceImpl implements PayService {

    private final PayRecordMapper payRecordMapper;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public PayRecord pay(Long userId, String orderNo, Integer payType) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo).eq(Order::getUserId, userId));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        if (order.getStatus() != OrderStatus.PENDING_PAY) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        // 模拟支付成功
        PayRecord record = new PayRecord();
        record.setOrderNo(orderNo);
        record.setUserId(userId);
        record.setPayAmount(order.getPayAmount());
        record.setPayType(payType != null ? payType : 1);
        record.setPayStatus(1);
        record.setTradeNo(IdUtil.getSnowflakeNextIdStr());
        record.setPayTime(LocalDateTime.now());
        payRecordMapper.insert(record);

        // 更新订单状态
        order.setStatus(OrderStatus.PAID);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        return record;
    }

    @Override
    public PayRecord queryPayStatus(String orderNo) {
        PayRecord record = payRecordMapper.selectOne(new LambdaQueryWrapper<PayRecord>()
                .eq(PayRecord::getOrderNo, orderNo)
                .orderByDesc(PayRecord::getCreateTime)
                .last("LIMIT 1"));
        if (record == null) {
            throw new BusinessException(ResultCode.PAY_RECORD_NOT_EXIST);
        }
        return record;
    }

    @Override
    @Transactional
    public void refund(Long userId, String orderNo) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo).eq(Order::getUserId, userId));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        // 更新支付记录
        PayRecord record = payRecordMapper.selectOne(new LambdaQueryWrapper<PayRecord>()
                .eq(PayRecord::getOrderNo, orderNo).eq(PayRecord::getPayStatus, 1)
                .orderByDesc(PayRecord::getCreateTime).last("LIMIT 1"));
        if (record != null) {
            record.setPayStatus(3);
            record.setRefundTime(LocalDateTime.now());
            payRecordMapper.updateById(record);
        }
    }
}