package com.ecommerce.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.mall.vo.CategoryVO;
import com.ecommerce.mall.vo.HomeVO;
import com.ecommerce.mall.vo.ProductVO;

import java.util.List;

public interface ProductService {
    HomeVO getHome();
    List<CategoryVO> getCategoryTree();
    Page<ProductVO> getProductList(Integer page, Integer size, Long categoryId, String keyword,
                                   String priceMin, String priceMax, String sort);
    ProductVO getProductDetail(Long spuId);
    Page<ProductVO> search(String keyword, Integer page, Integer size);
    void addFavorite(Long userId, Long spuId);
    void removeFavorite(Long userId, Long spuId);
    Page<ProductVO> getFavorites(Long userId, Integer page, Integer size);
    boolean isFavorite(Long userId, Long spuId);
}