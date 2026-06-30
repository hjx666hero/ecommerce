package com.ecommerce.mall.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartVO {
    private Long id;
    private Long userId;
    private Long skuId;
    private Long spuId;
    private String spuName;
    private String spuImage;
    private String skuSpec;
    private BigDecimal price;
    private Integer quantity;
    private Integer selected;
    private Integer stock;
    private LocalDateTime createTime;
}