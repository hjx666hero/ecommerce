package com.ecommerce.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.mall.entity.SeckillActivity;
import com.ecommerce.mall.mapper.SeckillActivityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminSeckillService {

    private final SeckillActivityMapper seckillActivityMapper;

    public Page<SeckillActivity> getSeckillList(Integer page, Integer size) {
        return seckillActivityMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SeckillActivity>().orderByDesc(SeckillActivity::getCreateTime));
    }

    @Transactional
    public void saveSeckill(SeckillActivity activity) {
        if (activity.getId() == null) {
            seckillActivityMapper.insert(activity);
        } else {
            seckillActivityMapper.updateById(activity);
        }
    }

    @Transactional
    public void deleteSeckill(Long id) {
        seckillActivityMapper.deleteById(id);
    }
}