package com.ecommerce.mall.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CartTotalVO {
    private List<CartVO> cartList;
    private Integer totalCount;
    private BigDecimal totalAmount;
}