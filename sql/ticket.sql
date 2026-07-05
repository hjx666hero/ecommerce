CREATE TABLE IF NOT EXISTS `ticket` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '工单ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(256) NOT NULL COMMENT '工单标题',
    `description` TEXT COMMENT '工单描述',
    `status` TINYINT DEFAULT 0 COMMENT '状态 0待处理 1处理中 2已解决 3已关闭',
    `reply` TEXT COMMENT '客服回复',
    `reply_time` DATETIME DEFAULT NULL COMMENT '回复时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后工单表';
