package com.ecommerce.mall.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemVO {
    private Long id;
    private Long orderId;
    private String orderNo;
    private Long spuId;
    private Long skuId;
    private String spuName;
    private String skuSpec;
    private String skuImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalAmount;
}