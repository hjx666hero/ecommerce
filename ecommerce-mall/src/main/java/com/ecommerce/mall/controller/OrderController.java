package com.ecommerce.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.result.Result;
import com.ecommerce.mall.dto.OrderCreateDTO;
import com.ecommerce.mall.service.OrderService;
import com.ecommerce.mall.vo.OrderVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public Result<OrderVO> createOrder(@Valid @RequestBody OrderCreateDTO orderCreateDTO, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(orderService.createOrder(userId, orderCreateDTO));
    }

    @GetMapping("/list")
    public Result<Page<OrderVO>> getOrderList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(orderService.getOrderList(userId, status, page, size));
    }

    @GetMapping("/detail/{orderId}")
    public Result<OrderVO> getOrderDetail(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(orderService.getOrderDetail(userId, orderId));
    }

    @GetMapping("/detail/no/{orderNo}")
    public Result<OrderVO> getOrderDetailByOrderNo(@PathVariable String orderNo, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(orderService.getOrderDetailByOrderNo(userId, orderNo));
    }

    @PutMapping("/cancel/{orderNo}")
    public Result<Void> cancelOrder(@PathVariable String orderNo, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.cancelOrderByOrderNo(userId, orderNo);
        return Result.success();
    }

    @PutMapping("/confirm/{orderNo}")
    public Result<Void> confirmReceive(@PathVariable String orderNo, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.confirmReceiveByOrderNo(userId, orderNo);
        return Result.success();
    }

    @PutMapping("/refund/{orderNo}")
    public Result<Void> applyRefund(@PathVariable String orderNo, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.applyRefundByOrderNo(userId, orderNo);
        return Result.success();
    }
}