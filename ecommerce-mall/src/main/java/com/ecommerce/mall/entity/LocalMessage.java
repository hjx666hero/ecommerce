package com.ecommerce.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("local_message")
public class LocalMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String topic;
    private String messageKey;
    private String content;
    private Integer status; // 0-待处理, 1-处理中, 2-已完成, 3-失败
    private Integer retryCount;
    private Integer maxRetry;
    private String errorMsg;
    private LocalDateTime nextRetryTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
