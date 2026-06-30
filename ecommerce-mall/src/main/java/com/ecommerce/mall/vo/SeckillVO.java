package com.ecommerce.mall.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SeckillVO {
    private Long id;
    private String name;
    private Long spuId;
    private Long skuId;
    private String spuName;
    private String spuImage;
    private String skuSpec;
    private BigDecimal originalPrice;
    private BigDecimal seckillPrice;
    private Integer stock;
    private Integer limitPerUser;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private Long remainSeconds;
}