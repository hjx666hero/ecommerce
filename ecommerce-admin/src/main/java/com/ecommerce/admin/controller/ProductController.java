package com.ecommerce.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.admin.service.AdminProductService;
import com.ecommerce.common.result.Result;
import com.ecommerce.mall.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/product")
@RequiredArgsConstructor
public class ProductController {

    private final AdminProductService adminProductService;

    // 分类
    @GetMapping("/category/list")
    public Result<Page<Category>> getCategoryList(@RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(adminProductService.getCategoryList(page, size));
    }

    @PostMapping("/category")
    public Result<Void> saveCategory(@RequestBody Category category) {
        adminProductService.saveCategory(category);
        return Result.success();
    }

    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        adminProductService.deleteCategory(id);
        return Result.success();
    }

    // 品牌
    @GetMapping("/brand/list")
    public Result<Page<Brand>> getBrandList(@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(adminProductService.getBrandList(page, size));
    }

    @PostMapping("/brand")
    public Result<Void> saveBrand(@RequestBody Brand brand) {
        adminProductService.saveBrand(brand);
        return Result.success();
    }

    @DeleteMapping("/brand/{id}")
    public Result<Void> deleteBrand(@PathVariable Long id) {
        adminProductService.deleteBrand(id);
        return Result.success();
    }

    // SPU
    @GetMapping("/spu/list")
    public Result<Page<Spu>> getSpuList(@RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer size,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Integer status) {
        return Result.success(adminProductService.getSpuList(page, size, keyword, status));
    }

    @PostMapping("/spu")
    public Result<Void> saveSpu(@RequestBody Spu spu) {
        adminProductService.saveSpu(spu);
        return Result.success();
    }

    @PutMapping("/spu/{spuId}/status")
    public Result<Void> updateSpuStatus(@PathVariable Long spuId, @RequestBody Map<String, Integer> params) {
        adminProductService.updateSpuStatus(spuId, params.get("status"));
        return Result.success();
    }

    @DeleteMapping("/spu/{spuId}")
    public Result<Void> deleteSpu(@PathVariable Long spuId) {
        adminProductService.deleteSpu(spuId);
        return Result.success();
    }

    // SKU
    @GetMapping("/sku/list")
    public Result<Page<Sku>> getSkuList(@RequestParam Long spuId,
                                         @RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(adminProductService.getSkuList(spuId, page, size));
    }

    @PostMapping("/sku")
    public Result<Void> saveSku(@RequestBody Sku sku) {
        adminProductService.saveSku(sku);
        return Result.success();
    }

    @DeleteMapping("/sku/{skuId}")
    public Result<Void> deleteSku(@PathVariable Long skuId) {
        adminProductService.deleteSku(skuId);
        return Result.success();
    }
}