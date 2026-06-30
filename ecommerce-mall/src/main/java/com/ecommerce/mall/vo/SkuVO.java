package com.ecommerce.mall.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SkuVO {
    private Long id;
    private String skuCode;
    private String specName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private String image;
}