package com.ecommerce.mall.service;

import com.ecommerce.mall.vo.CartVO;
import java.util.List;

public interface CartService {
    void addToCart(Long userId, Long skuId, Long productId, Integer quantity);
    void updateQuantity(Long userId, Long cartId, Integer quantity);
    void deleteCart(Long userId, Long cartId);
    void deleteBatch(Long userId, List<Long> cartIds);
    void deleteAll(Long userId);
    void selectAll(Long userId, Boolean selected);
    void selectOne(Long userId, Long cartId, Boolean selected);
    List<CartVO> getCartList(Long userId);
    List<CartVO> getSelectedCartList(Long userId);
}