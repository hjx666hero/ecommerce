package com.ecommerce.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.admin.service.AdminOrderService;
import com.ecommerce.common.result.Result;
import com.ecommerce.mall.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/order")
@RequiredArgsConstructor
public class OrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping("/list")
    public Result<Page<Order>> getOrderList(@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             @RequestParam(required = false) Integer status,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String orderNo) {
        return Result.success(adminOrderService.getOrderList(page, size, status, keyword, orderNo));
    }

    @GetMapping("/detail/{orderId}")
    public Result<Order> getOrderDetail(@PathVariable Long orderId) {
        return Result.success(adminOrderService.getOrderDetail(orderId));
    }

    @PutMapping("/deliver/{orderId}")
    public Result<Void> deliverOrder(@PathVariable Long orderId, @RequestBody Map<String, String> params) {
        adminOrderService.deliverOrder(orderId, params.get("logisticsCompany"), params.get("logisticsNo"));
        return Result.success();
    }

    @PutMapping("/refund/{orderId}")
    public Result<Void> handleRefund(@PathVariable Long orderId) {
        adminOrderService.handleRefund(orderId);
        return Result.success();
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getOrderStatistics() {
        return Result.success(adminOrderService.getOrderStatistics());
    }
}