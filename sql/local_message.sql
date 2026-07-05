-- ============================================
-- 本地消息表（最终一致性保证）
-- ============================================
USE ecommerce;

CREATE TABLE IF NOT EXISTS `local_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `topic` VARCHAR(100) NOT NULL COMMENT '消息主题（如: order_stock_rollback, order_timeout_cancel）',
    `message_key` VARCHAR(200) NOT NULL COMMENT '消息唯一键（防重复）',
    `content` JSON COMMENT '消息内容',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待处理, 1-处理中, 2-已完成, 3-失败',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `max_retry` INT NOT NULL DEFAULT 5 COMMENT '最大重试次数',
    `error_msg` VARCHAR(500) COMMENT '错误信息',
    `next_retry_time` DATETIME COMMENT '下次重试时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_message_key` (`message_key`),
    KEY `idx_status_retry` (`status`, `next_retry_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='本地消息表';
