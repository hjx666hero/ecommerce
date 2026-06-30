package com.ecommerce.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.constant.OrderStatus;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.result.ResultCode;
import com.ecommerce.mall.entity.Order;
import com.ecommerce.mall.entity.OrderItem;
import com.ecommerce.mall.entity.ProductReview;
import com.ecommerce.mall.entity.User;
import com.ecommerce.mall.mapper.OrderItemMapper;
import com.ecommerce.mall.mapper.OrderMapper;
import com.ecommerce.mall.mapper.ProductReviewMapper;
import com.ecommerce.mall.mapper.UserMapper;
import com.ecommerce.mall.service.ReviewService;
import com.ecommerce.mall.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ProductReviewMapper reviewMapper;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    public Page<ReviewVO> getReviews(Long spuId, Integer page, Integer size) {
        try {
            Page<ProductReview> reviewPage = reviewMapper.selectPage(
                    new Page<>(page, size),
                    new LambdaQueryWrapper<ProductReview>()
                            .eq(ProductReview::getSpuId, spuId)
                            .eq(ProductReview::getStatus, 1)
                            .orderByDesc(ProductReview::getCreateTime));

            Page<ReviewVO> voPage = new Page<>(reviewPage.getCurrent(), reviewPage.getSize(), reviewPage.getTotal());
            voPage.setRecords(reviewPage.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
            return voPage;
        } catch (Exception e) {
            log.warn("查询商品评价失败, spuId={}, error={}", spuId, e.getMessage());
            return new Page<>(page, size, 0);
        }
    }

    @Override
    public void addReview(Long userId, Long spuId, Long orderId, Integer rating, String content) {
        // 验证用户是否已购买该商品（订单状态为已完成=3）
        List<Order> completedOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getStatus, OrderStatus.COMPLETED));
        List<Long> completedOrderIds = completedOrders.stream().map(Order::getId).collect(Collectors.toList());

        boolean hasPurchased = false;
        if (!completedOrderIds.isEmpty()) {
            hasPurchased = orderItemMapper.selectCount(new LambdaQueryWrapper<OrderItem>()
                    .in(OrderItem::getOrderId, completedOrderIds)
                    .eq(OrderItem::getSpuId, spuId)) > 0;
        }

        if (!hasPurchased) {
            throw new BusinessException(ResultCode.ORDER_NOT_PURCHASED);
        }

        User user = userMapper.selectById(userId);
        ProductReview review = new ProductReview();
        review.setSpuId(spuId);
        review.setOrderId(orderId);
        review.setUserId(userId);
        review.setNickname(user != null ? user.getNickname() : "匿名用户");
        review.setAvatar(user != null ? user.getAvatar() : null);
        review.setRating(rating != null ? rating : 5);
        review.setContent(content);
        review.setStatus(1);
        reviewMapper.insert(review);
    }

    private ReviewVO toVO(ProductReview review) {
        ReviewVO vo = new ReviewVO();
        vo.setId(review.getId());
        vo.setSpuId(review.getSpuId());
        vo.setUserId(review.getUserId());
        vo.setNickname(review.getNickname());
        vo.setAvatar(review.getAvatar());
        vo.setRating(review.getRating());
        vo.setContent(review.getContent());
        if (review.getImages() != null && !review.getImages().isEmpty()) {
            vo.setImages(Arrays.asList(review.getImages().split(",")));
        } else {
            vo.setImages(Collections.emptyList());
        }
        vo.setReply(review.getReply());
        vo.setReplyTime(review.getReplyTime());
        vo.setCreateTime(review.getCreateTime());
        return vo;
    }
}