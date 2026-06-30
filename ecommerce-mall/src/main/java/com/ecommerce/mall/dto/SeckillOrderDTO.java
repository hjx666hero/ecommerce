package com.ecommerce.mall.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SeckillOrderDTO {
    @NotNull(message = "秒杀活动ID不能为空")
    private Long activityId;

    @NotNull(message = "地址ID不能为空")
    private Long addressId;
}