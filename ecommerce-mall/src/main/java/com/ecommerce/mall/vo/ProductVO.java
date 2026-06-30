package com.ecommerce.mall.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductVO {
    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private String description;
    private String detail;
    private String mainImage;
    private List<String> images;
    private String specParams;
    private Integer sales;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<SkuVO> skuList;
}