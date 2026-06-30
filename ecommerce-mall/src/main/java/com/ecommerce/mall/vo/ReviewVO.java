package com.ecommerce.mall.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewVO {
    private Long id;
    private Long spuId;
    private Long userId;
    private String nickname;
    private String avatar;
    private Integer rating;
    private String content;
    private List<String> images;
    private String reply;
    private LocalDateTime replyTime;
    private LocalDateTime createTime;
}