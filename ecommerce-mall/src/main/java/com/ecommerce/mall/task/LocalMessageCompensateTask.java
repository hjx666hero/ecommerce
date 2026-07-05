package com.ecommerce.mall.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.mall.entity.LocalMessage;
import com.ecommerce.mall.entity.Order;
import com.ecommerce.mall.entity.OrderItem;
import com.ecommerce.mall.mapper.LocalMessageMapper;
import com.ecommerce.mall.mapper.OrderItemMapper;
import com.ecommerce.mall.mapper.OrderMapper;
import com.ecommerce.mall.mapper.SkuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 本地消息补偿重试定时任务
 * 每1分钟扫描待处理/失败的消息，进行补偿重试
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalMessageCompensateTask {

    private final LocalMessageMapper localMessageMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final SkuMapper skuMapper;

    @Scheduled(fixedDelay = 60000) // 每1分钟
    @Transactional
    public void compensate() {
        // 查询待处理的消息（重试时间已到，且未超过最大重试次数）
        List<LocalMessage> messages = localMessageMapper.selectList(new LambdaQueryWrapper<LocalMessage>()
                .eq(LocalMessage::getStatus, 0)
                .le(LocalMessage::getNextRetryTime, LocalDateTime.now())
                .apply("retry_count < max_retry")
                .last("LIMIT 20"));

        if (messages.isEmpty()) {
            return;
        }

        log.info("发现 {} 条待补偿消息", messages.size());

        for (LocalMessage msg : messages) {
            // CAS标记为处理中，防止重复处理
            int affected = localMessageMapper.markProcessing(msg.getId());
            if (affected == 0) continue;

            try {
                processMessage(msg);
                msg.setStatus(2); // 已完成
                localMessageMapper.updateById(msg);
            } catch (Exception e) {
                log.error("消息补偿失败: id={}, topic={}, error={}", msg.getId(), msg.getTopic(), e.getMessage());
                msg.setStatus(0); // 重置为待处理
                msg.setErrorMsg(e.getMessage());
                msg.setNextRetryTime(LocalDateTime.now().plusMinutes(2)); // 2分钟后重试
                localMessageMapper.updateById(msg);
            }
        }
    }

    private void processMessage(LocalMessage msg) {
        switch (msg.getTopic()) {
            case "order_stock_rollback":
                handleStockRollback(msg);
                break;
            default:
                log.warn("未知消息主题: {}", msg.getTopic());
                msg.setStatus(3);
                localMessageMapper.updateById(msg);
        }
    }

    /**
     * 处理库存回滚补偿消息
     */
    private void handleStockRollback(LocalMessage msg) {
        // 消息内容格式: {"orderId": 123}
        String content = msg.getContent();
        try {
            // 简单解析JSON（hutool可替换为更健壮的解析）
            long orderId = Long.parseLong(content.replaceAll("\\D", ""));
            List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                    .eq(OrderItem::getOrderId, orderId));
            for (OrderItem item : items) {
                skuMapper.rollbackStock(item.getSkuId(), item.getQuantity());
            }
            log.info("库存回滚补偿完成: orderId={}, messageId={}", orderId, msg.getId());
        } catch (Exception e) {
            throw new RuntimeException("库存回滚补偿失败: " + e.getMessage(), e);
        }
    }
}
