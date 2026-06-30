package com.ecommerce.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.mall.dto.SeckillOrderDTO;
import com.ecommerce.mall.vo.SeckillVO;

public interface SeckillService {
    Page<SeckillVO> getSeckillList(Integer page, Integer size);
    SeckillVO getSeckillDetail(Long activityId);
    String createSeckillOrder(Long userId, SeckillOrderDTO seckillOrderDTO);
}