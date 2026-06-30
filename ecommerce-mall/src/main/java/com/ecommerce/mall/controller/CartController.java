package com.ecommerce.mall.controller;

import com.ecommerce.common.result.Result;
import com.ecommerce.mall.service.CartService;
import com.ecommerce.mall.vo.CartVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/list")
    public Result<List<CartVO>> getCartList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(cartService.getCartList(userId));
    }

    @GetMapping("/selected")
    public Result<List<CartVO>> getSelectedCartList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(cartService.getSelectedCartList(userId));
    }

    @PostMapping("/add")
    public Result<Void> addToCart(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long skuId = params.get("skuId") != null ? ((Number) params.get("skuId")).longValue() : null;
        Long productId = params.get("productId") != null ? ((Number) params.get("productId")).longValue() : null;
        Integer quantity = params.get("quantity") != null ? ((Number) params.get("quantity")).intValue() : 1;
        cartService.addToCart(userId, skuId, productId, quantity);
        return Result.success();
    }

    @PutMapping("/{cartId}/quantity")
    public Result<Void> updateQuantity(@PathVariable Long cartId, @RequestBody Map<String, Integer> params,
                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.updateQuantity(userId, cartId, params.get("quantity"));
        return Result.success();
    }

    @DeleteMapping("/{cartId}")
    public Result<Void> deleteCart(@PathVariable Long cartId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.deleteCart(userId, cartId);
        return Result.success();
    }

    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@RequestBody Map<String, List<Long>> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.deleteBatch(userId, params.get("ids"));
        return Result.success();
    }

    @DeleteMapping("/clear")
    public Result<Void> clearCart(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.deleteAll(userId);
        return Result.success();
    }

    @PutMapping("/select-all")
    public Result<Void> selectAll(@RequestBody Map<String, Boolean> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.selectAll(userId, params.get("selected"));
        return Result.success();
    }

    @PutMapping("/{cartId}/select")
    public Result<Void> selectOne(@PathVariable Long cartId, @RequestBody Map<String, Boolean> params,
                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.selectOne(userId, cartId, params.get("selected"));
        return Result.success();
    }
}