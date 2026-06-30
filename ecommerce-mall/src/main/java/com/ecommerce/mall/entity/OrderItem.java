package com.ecommerce.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_item")
public class OrderItem {
    @TableId(type = IdType.AUTO)
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}