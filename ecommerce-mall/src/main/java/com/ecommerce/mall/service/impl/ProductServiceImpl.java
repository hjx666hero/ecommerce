package com.ecommerce.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.result.ResultCode;
import com.ecommerce.mall.entity.*;
import com.ecommerce.mall.mapper.*;
import com.ecommerce.mall.service.ProductService;
import com.ecommerce.mall.vo.*;
import com.ecommerce.mall.vo.SkuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final CarouselMapper carouselMapper;
    private final SpuMapper spuMapper;
    private final SkuMapper skuMapper;
    private final CategoryMapper categoryMapper;
    private final BrandMapper brandMapper;
    private final AnnouncementMapper announcementMapper;
    private final UserFavoriteMapper userFavoriteMapper;

    @Override
    public HomeVO getHome() {
        HomeVO homeVO = new HomeVO();

        // 轮播图
        List<Carousel> carousels = carouselMapper.selectList(new LambdaQueryWrapper<Carousel>()
                .eq(Carousel::getStatus, 1)
                .orderByAsc(Carousel::getSortOrder));
        homeVO.setCarousels(carousels.stream().map(c -> {
            CarouselVO cv = new CarouselVO();
            cv.setId(c.getId());
            cv.setTitle(c.getTitle());
            cv.setImageUrl(c.getImageUrl());
            cv.setLinkUrl(c.getLinkUrl());
            return cv;
        }).collect(Collectors.toList()));

        // 热门商品
        List<Spu> hotSpus = spuMapper.selectList(new LambdaQueryWrapper<Spu>()
                .eq(Spu::getStatus, 1).eq(Spu::getIsHot, 1)
                .orderByDesc(Spu::getSales).last("LIMIT 8"));
        homeVO.setHotProducts(toProductVOList(hotSpus));

        // 新品
        List<Spu> newSpus = spuMapper.selectList(new LambdaQueryWrapper<Spu>()
                .eq(Spu::getStatus, 1).eq(Spu::getIsNew, 1)
                .orderByDesc(Spu::getCreateTime).last("LIMIT 8"));
        homeVO.setNewProducts(toProductVOList(newSpus));

        // 一级分类
        List<Category> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, 0L).eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder));
        homeVO.setCategories(categories.stream().map(c -> {
            CategoryVO cv = new CategoryVO();
            cv.setId(c.getId());
            cv.setName(c.getName());
            cv.setIcon(c.getIcon());
            cv.setSortOrder(c.getSortOrder());
            return cv;
        }).collect(Collectors.toList()));

        // 公告
        List<Announcement> announcements = announcementMapper.selectList(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, 1)
                .orderByDesc(Announcement::getCreateTime).last("LIMIT 5"));
        homeVO.setAnnouncements(announcements.stream().map(a -> {
            AnnouncementVO av = new AnnouncementVO();
            av.setId(a.getId());
            av.setTitle(a.getTitle());
            av.setType(a.getType());
            return av;
        }).collect(Collectors.toList()));

        return homeVO;
    }

    @Override
    public List<CategoryVO> getCategoryTree() {
        List<Category> allCategories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder));
        Map<Long, List<CategoryVO>> parentMap = new HashMap<>();
        List<CategoryVO> roots = new ArrayList<>();
        for (Category category : allCategories) {
            CategoryVO vo = new CategoryVO();
            vo.setId(category.getId());
            vo.setParentId(category.getParentId());
            vo.setName(category.getName());
            vo.setIcon(category.getIcon());
            vo.setSortOrder(category.getSortOrder());
            vo.setChildren(new ArrayList<>());
            if (category.getParentId() == 0) {
                roots.add(vo);
            }
            parentMap.computeIfAbsent(category.getParentId(), k -> new ArrayList<>()).add(vo);
        }
        // 构建树
        for (CategoryVO vo : allCategories.stream().map(c -> {
            CategoryVO v = new CategoryVO();
            v.setId(c.getId());
            v.setParentId(c.getParentId());
            v.setName(c.getName());
            v.setIcon(c.getIcon());
            v.setSortOrder(c.getSortOrder());
            return v;
        }).collect(Collectors.toList())) {
            List<CategoryVO> children = parentMap.get(vo.getId());
            if (children != null) {
                vo.setChildren(children);
            }
        }
        // 重建树
        List<CategoryVO> tree = new ArrayList<>();
        for (Category category : allCategories) {
            if (category.getParentId() == 0) {
                CategoryVO vo = new CategoryVO();
                vo.setId(category.getId());
                vo.setParentId(category.getParentId());
                vo.setName(category.getName());
                vo.setIcon(category.getIcon());
                vo.setSortOrder(category.getSortOrder());
                vo.setChildren(buildChildren(category.getId(), allCategories));
                tree.add(vo);
            }
        }
        return tree;
    }

    private List<CategoryVO> buildChildren(Long parentId, List<Category> allCategories) {
        List<CategoryVO> children = new ArrayList<>();
        for (Category category : allCategories) {
            if (category.getParentId().equals(parentId)) {
                CategoryVO vo = new CategoryVO();
                vo.setId(category.getId());
                vo.setParentId(category.getParentId());
                vo.setName(category.getName());
                vo.setIcon(category.getIcon());
                vo.setSortOrder(category.getSortOrder());
                vo.setChildren(buildChildren(category.getId(), allCategories));
                children.add(vo);
            }
        }
        return children;
    }

    @Override
    public Page<ProductVO> getProductList(Integer page, Integer size, Long categoryId, String keyword,
                                          String priceMin, String priceMax, String sort) {
        LambdaQueryWrapper<Spu> wrapper = new LambdaQueryWrapper<Spu>()
                .eq(Spu::getStatus, 1);

        if (categoryId != null) {
            // 查询该分类及其所有子分类
            List<Long> categoryIds = getAllChildCategoryIds(categoryId);
            categoryIds.add(categoryId);
            wrapper.in(Spu::getCategoryId, categoryIds);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Spu::getName, keyword);
        }

        // 排序
        if ("price_asc".equals(sort)) {
            wrapper.orderByAsc(Spu::getId);
        } else if ("price_desc".equals(sort)) {
            wrapper.orderByDesc(Spu::getId);
        } else if ("sales".equals(sort)) {
            wrapper.orderByDesc(Spu::getSales);
        } else {
            wrapper.orderByDesc(Spu::getCreateTime);
        }

        Page<Spu> spuPage = spuMapper.selectPage(new Page<>(page, size), wrapper);
        List<ProductVO> voList = toProductVOList(spuPage.getRecords());

        // 价格区间过滤
        if (priceMin != null || priceMax != null) {
            BigDecimal min = priceMin != null ? new BigDecimal(priceMin) : BigDecimal.ZERO;
            BigDecimal max = priceMax != null ? new BigDecimal(priceMax) : new BigDecimal("999999");
            voList = voList.stream()
                    .filter(vo -> vo.getMinPrice().compareTo(max) <= 0 && vo.getMaxPrice().compareTo(min) >= 0)
                    .collect(Collectors.toList());
        }

        Page<ProductVO> resultPage = new Page<>(spuPage.getCurrent(), spuPage.getSize(), spuPage.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }

    @Override
    public ProductVO getProductDetail(Long spuId) {
        Spu spu = spuMapper.selectById(spuId);
        if (spu == null || spu.getStatus() == 0) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }
        ProductVO vo = toProductVO(spu);
        // SKU列表
        List<Sku> skus = skuMapper.selectList(new LambdaQueryWrapper<Sku>()
                .eq(Sku::getSpuId, spuId).eq(Sku::getStatus, 1));
        vo.setSkuList(skus.stream().map(sku -> {
            SkuVO sv = new SkuVO();
            sv.setId(sku.getId());
            sv.setSkuCode(sku.getSkuCode());
            sv.setSpecName(sku.getSpecName());
            sv.setPrice(sku.getPrice());
            sv.setOriginalPrice(sku.getOriginalPrice());
            sv.setStock(sku.getStock());
            sv.setImage(sku.getImage());
            return sv;
        }).collect(Collectors.toList()));
        return vo;
    }

    @Override
    public Page<ProductVO> search(String keyword, Integer page, Integer size) {
        return getProductList(page, size, null, keyword, null, null, null);
    }

    @Override
    public void addFavorite(Long userId, Long spuId) {
        Spu spu = spuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }
        if (userFavoriteMapper.selectCount(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId).eq(UserFavorite::getSpuId, spuId)) > 0) {
            return;
        }
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setSpuId(spuId);
        userFavoriteMapper.insert(favorite);
    }

    @Override
    public void removeFavorite(Long userId, Long spuId) {
        userFavoriteMapper.delete(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId).eq(UserFavorite::getSpuId, spuId));
    }

    @Override
    public Page<ProductVO> getFavorites(Long userId, Integer page, Integer size) {
        Page<UserFavorite> favPage = userFavoriteMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .orderByDesc(UserFavorite::getCreateTime));
        List<Long> spuIds = favPage.getRecords().stream().map(UserFavorite::getSpuId).collect(Collectors.toList());
        if (spuIds.isEmpty()) {
            return new Page<>(page, size, 0);
        }
        List<Spu> spus = spuMapper.selectBatchIds(spuIds);
        Map<Long, Spu> spuMap = spus.stream().collect(Collectors.toMap(Spu::getId, s -> s));
        List<ProductVO> voList = spuIds.stream()
                .filter(spuMap::containsKey)
                .map(id -> toProductVO(spuMap.get(id)))
                .collect(Collectors.toList());
        Page<ProductVO> result = new Page<>(favPage.getCurrent(), favPage.getSize(), favPage.getTotal());
        result.setRecords(voList);
        return result;
    }

    @Override
    public boolean isFavorite(Long userId, Long spuId) {
        return userFavoriteMapper.selectCount(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId).eq(UserFavorite::getSpuId, spuId)) > 0;
    }

    private List<ProductVO> toProductVOList(List<Spu> spus) {
        if (spus.isEmpty()) return Collections.emptyList();
        List<Long> spuIds = spus.stream().map(Spu::getId).collect(Collectors.toList());
        Map<Long, List<Sku>> skuMap = skuMapper.selectList(new LambdaQueryWrapper<Sku>()
                .in(Sku::getSpuId, spuIds).eq(Sku::getStatus, 1))
                .stream().collect(Collectors.groupingBy(Sku::getSpuId));
        return spus.stream().map(spu -> {
            ProductVO vo = toProductVO(spu);
            List<Sku> skus = skuMap.getOrDefault(spu.getId(), Collections.emptyList());
            vo.setSkuList(skus.stream().map(sku -> {
                SkuVO sv = new SkuVO();
                sv.setId(sku.getId());
                sv.setSkuCode(sku.getSkuCode());
                sv.setSpecName(sku.getSpecName());
                sv.setPrice(sku.getPrice());
                sv.setOriginalPrice(sku.getOriginalPrice());
                sv.setStock(sku.getStock());
                sv.setImage(sku.getImage());
                return sv;
            }).collect(Collectors.toList()));
            
            // 从SKU列表计算最低和最高价格
            if (!skus.isEmpty()) {
                BigDecimal minPrice = skus.stream()
                        .map(Sku::getPrice)
                        .filter(Objects::nonNull)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);
                BigDecimal maxPrice = skus.stream()
                        .map(Sku::getPrice)
                        .filter(Objects::nonNull)
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);
                vo.setMinPrice(minPrice);
                vo.setMaxPrice(maxPrice);
            } else {
                vo.setMinPrice(BigDecimal.ZERO);
                vo.setMaxPrice(BigDecimal.ZERO);
            }
            
            return vo;
        }).collect(Collectors.toList());
    }

    private ProductVO toProductVO(Spu spu) {
        ProductVO vo = new ProductVO();
        vo.setId(spu.getId());
        vo.setName(spu.getName());
        vo.setCategoryId(spu.getCategoryId());
        vo.setBrandId(spu.getBrandId());
        vo.setDescription(spu.getDescription());
        vo.setDetail(spu.getDetail());
        vo.setMainImage(spu.getMainImage());
        vo.setSpecParams(spu.getSpecParams());
        vo.setSales(spu.getSales());
        if (spu.getImages() != null) {
            try {
                vo.setImages(Arrays.asList(spu.getImages().split(",")));
            } catch (Exception ignored) {
                vo.setImages(Collections.emptyList());
            }
        }
        return vo;
    }

    private List<Long> getAllChildCategoryIds(Long parentId) {
        List<Long> ids = new ArrayList<>();
        List<Category> children = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, parentId));
        for (Category child : children) {
            ids.add(child.getId());
            ids.addAll(getAllChildCategoryIds(child.getId()));
        }
        return ids;
    }
}