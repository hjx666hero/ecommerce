package com.ecommerce.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.mall.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}