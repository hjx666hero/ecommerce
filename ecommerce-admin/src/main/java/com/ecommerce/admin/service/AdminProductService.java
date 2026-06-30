package com.ecommerce.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.mall.entity.*;
import com.ecommerce.mall.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final CategoryMapper categoryMapper;
    private final BrandMapper brandMapper;
    private final SpuMapper spuMapper;
    private final SkuMapper skuMapper;

    // 分类管理
    public Page<Category> getCategoryList(Integer page, Integer size) {
        return categoryMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder));
    }

    public void saveCategory(Category category) {
        if (category.getId() == null) {
            categoryMapper.insert(category);
        } else {
            categoryMapper.updateById(category);
        }
    }

    public void deleteCategory(Long id) {
        categoryMapper.deleteById(id);
    }

    // 品牌管理
    public Page<Brand> getBrandList(Integer page, Integer size) {
        return brandMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Brand>().orderByAsc(Brand::getSortOrder));
    }

    public void saveBrand(Brand brand) {
        if (brand.getId() == null) {
            brandMapper.insert(brand);
        } else {
            brandMapper.updateById(brand);
        }
    }

    public void deleteBrand(Long id) {
        brandMapper.deleteById(id);
    }

    // SPU管理
    public Page<Spu> getSpuList(Integer page, Integer size, String keyword, Integer status) {
        LambdaQueryWrapper<Spu> wrapper = new LambdaQueryWrapper<Spu>()
                .orderByDesc(Spu::getCreateTime);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Spu::getName, keyword);
        }
        if (status != null) {
            wrapper.eq(Spu::getStatus, status);
        }
        return spuMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Transactional
    public void saveSpu(Spu spu) {
        if (spu.getId() == null) {
            spuMapper.insert(spu);
        } else {
            spuMapper.updateById(spu);
        }
    }

    @Transactional
    public void updateSpuStatus(Long spuId, Integer status) {
        Spu spu = spuMapper.selectById(spuId);
        if (spu != null) {
            spu.setStatus(status);
            spuMapper.updateById(spu);
        }
    }

    public void deleteSpu(Long spuId) {
        spuMapper.deleteById(spuId);
    }

    // SKU管理
    public Page<Sku> getSkuList(Long spuId, Integer page, Integer size) {
        return skuMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, spuId));
    }

    @Transactional
    public void saveSku(Sku sku) {
        if (sku.getId() == null) {
            skuMapper.insert(sku);
        } else {
            skuMapper.updateById(sku);
        }
    }

    public void deleteSku(Long skuId) {
        skuMapper.deleteById(skuId);
    }
}