package com.ecommerce.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.result.Result;
import com.ecommerce.mall.service.ReviewService;
import com.ecommerce.mall.vo.ReviewVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/list/{spuId}")
    public Result<Page<ReviewVO>> getReviews(
            @PathVariable Long spuId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(reviewService.getReviews(spuId, page, size));
    }

    @PostMapping("/add")
    public Result<Void> addReview(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long spuId = ((Number) params.get("spuId")).longValue();
        Long orderId = params.get("orderId") != null ? ((Number) params.get("orderId")).longValue() : null;
        Integer rating = params.get("rating") != null ? ((Number) params.get("rating")).intValue() : 5;
        String content = (String) params.get("content");
        reviewService.addReview(userId, spuId, orderId, rating, content);
        return Result.success();
    }
}