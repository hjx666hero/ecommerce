package com.ecommerce.mall.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageVO {
    private Long id;
    private String title;
    private String content;
    private Integer type;
    private String typeText;
    private Integer isRead;
    private LocalDateTime readTime;
    private LocalDateTime createTime;
}