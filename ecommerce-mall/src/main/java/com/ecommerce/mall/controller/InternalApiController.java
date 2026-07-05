package com.ecommerce.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.common.result.Result;
import com.ecommerce.common.result.ResultCode;
import com.ecommerce.mall.entity.Order;
import com.ecommerce.mall.entity.Ticket;
import com.ecommerce.mall.mapper.OrderMapper;
import com.ecommerce.mall.mapper.TicketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 内部业务API - 供Python Agent服务调用（仅内网可达）
 */
@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalApiController {

    private final OrderMapper orderMapper;
    private final TicketMapper ticketMapper;

    /**
     * 根据订单ID查询订单详情（含物流信息）
     */
    @GetMapping("/order/{orderId}")
    public Result<Map<String, Object>> getOrderDetail(@PathVariable Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.error(ResultCode.ORDER_NOT_EXIST);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderNo", order.getOrderNo());
        data.put("status", order.getStatus());
        data.put("statusText", getStatusText(order.getStatus()));
        data.put("totalAmount", order.getTotalAmount());
        data.put("payAmount", order.getPayAmount());
        data.put("receiverName", order.getReceiverName());
        data.put("receiverPhone", order.getReceiverPhone());
        data.put("receiverAddress", order.getReceiverAddress());
        data.put("logisticsCompany", order.getLogisticsCompany());
        data.put("logisticsNo", order.getLogisticsNo());
        data.put("payTime", order.getPayTime());
        data.put("deliveryTime", order.getDeliveryTime());
        data.put("completeTime", order.getCompleteTime());

        return Result.success(data);
    }

    /**
     * 创建售后工单
     */
    @PostMapping("/ticket")
    public Result<Map<String, Object>> createTicket(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("user_id").toString());
        String title = (String) body.get("title");
        String description = (String) body.get("description");

        Ticket ticket = new Ticket();
        ticket.setUserId(userId);
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setStatus(0);
        ticketMapper.insert(ticket);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ticketId", ticket.getId());
        data.put("title", ticket.getTitle());
        data.put("status", 0);
        return Result.success(data);
    }

    private String getStatusText(Integer status) {
        return switch (status) {
            case 0 -> "待付款";
            case 1 -> "已支付";
            case 2 -> "已发货";
            case 3 -> "已完成";
            case 4 -> "已取消";
            case 5 -> "已退款";
            default -> "未知";
        };
    }
}
