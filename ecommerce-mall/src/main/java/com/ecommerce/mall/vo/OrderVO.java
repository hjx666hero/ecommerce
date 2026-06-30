package com.ecommerce.mall.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private Integer status;
    private String statusText;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private Long couponId;
    private BigDecimal couponDiscount;
    private String logisticsCompany;
    private String logisticsNo;
    private LocalDateTime payTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime completeTime;
    private LocalDateTime cancelTime;
    private LocalDateTime createTime;
    private List<OrderItemVO> items;
}