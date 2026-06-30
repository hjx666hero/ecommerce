package com.ecommerce.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.mall.vo.ReviewVO;

public interface ReviewService {
    Page<ReviewVO> getReviews(Long spuId, Integer page, Integer size);
    void addReview(Long userId, Long spuId, Long orderId, Integer rating, String content);
}