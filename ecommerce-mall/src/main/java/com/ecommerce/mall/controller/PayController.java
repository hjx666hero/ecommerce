package com.ecommerce.mall.controller;

import com.ecommerce.common.result.Result;
import com.ecommerce.mall.entity.PayRecord;
import com.ecommerce.mall.service.PayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayController {

    private final PayService payService;

    @PostMapping("/pay")
    public Result<PayRecord> pay(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String orderNo = (String) params.get("orderNo");
        Integer payType = params.get("payType") != null ? ((Number) params.get("payType")).intValue() : 1;
        return Result.success(payService.pay(userId, orderNo, payType));
    }

    @GetMapping("/query/{orderNo}")
    public Result<PayRecord> queryPayStatus(@PathVariable String orderNo) {
        return Result.success(payService.queryPayStatus(orderNo));
    }

    @PostMapping("/refund")
    public Result<Void> refund(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        payService.refund(userId, params.get("orderNo"));
        return Result.success();
    }
}