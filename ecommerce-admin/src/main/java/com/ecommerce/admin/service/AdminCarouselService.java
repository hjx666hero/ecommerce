package com.ecommerce.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.mall.entity.Carousel;
import com.ecommerce.mall.mapper.CarouselMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminCarouselService {

    private final CarouselMapper carouselMapper;

    public Page<Carousel> getCarouselList(Integer page, Integer size) {
        return carouselMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Carousel>().orderByAsc(Carousel::getSortOrder));
    }

    @Transactional
    public void saveCarousel(Carousel carousel) {
        if (carousel.getId() == null) {
            carouselMapper.insert(carousel);
        } else {
            carouselMapper.updateById(carousel);
        }
    }

    @Transactional
    public void deleteCarousel(Long id) {
        carouselMapper.deleteById(id);
    }
}