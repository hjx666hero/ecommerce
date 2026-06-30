package com.ecommerce.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.result.Result;
import com.ecommerce.mall.service.ProductService;
import com.ecommerce.mall.vo.CategoryVO;
import com.ecommerce.mall.vo.ProductVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/category/tree")
    public Result<List<CategoryVO>> getCategoryTree() {
        return Result.success(productService.getCategoryTree());
    }

    @GetMapping("/list")
    public Result<Page<ProductVO>> getProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String priceMin,
            @RequestParam(required = false) String priceMax,
            @RequestParam(required = false) String sort) {
        return Result.success(productService.getProductList(page, size, categoryId, keyword, priceMin, priceMax, sort));
    }

    @GetMapping("/detail/{id}")
    public Result<ProductVO> getProductDetail(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id));
    }

    @GetMapping("/search")
    public Result<Page<ProductVO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(productService.search(keyword, page, size));
    }

    @PostMapping("/favorite/{spuId}")
    public Result<Void> addFavorite(@PathVariable Long spuId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        productService.addFavorite(userId, spuId);
        return Result.success();
    }

    @DeleteMapping("/favorite/{spuId}")
    public Result<Void> removeFavorite(@PathVariable Long spuId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        productService.removeFavorite(userId, spuId);
        return Result.success();
    }

    @GetMapping("/favorite/list")
    public Result<Page<ProductVO>> getFavorites(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(productService.getFavorites(userId, page, size));
    }

    @GetMapping("/favorite/check/{spuId}")
    public Result<Boolean> isFavorite(@PathVariable Long spuId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(productService.isFavorite(userId, spuId));
    }
}