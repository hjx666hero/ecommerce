package com.ecommerce.mall.service;

import com.ecommerce.mall.entity.PayRecord;

public interface PayService {
    PayRecord pay(Long userId, String orderNo, Integer payType);
    PayRecord queryPayStatus(String orderNo);
    void refund(Long userId, String orderNo);
}