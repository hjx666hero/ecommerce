package com.ecommerce.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.constant.RedisKey;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.result.ResultCode;
import com.ecommerce.common.util.RedisUtil;
import com.ecommerce.mall.entity.*;
import com.ecommerce.mall.mapper.*;
import com.ecommerce.mall.service.ProductService;
import com.ecommerce.mall.vo.*;
import com.ecommerce.mall.vo.SkuVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
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
    private final RedisUtil redisUtil;

    @Override
    public HomeVO getHome() {
        // 优先从Redis缓存获取（Redis故障时回退到数据库）
        try {
            HomeVO cached = redisUtil.get(RedisKey.HOME_DATA, HomeVO.class);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            log.warn("Redis获取首页缓存失败，回退到数据库查询: {}", e.getMessage());
        }

        HomeVO homeVO = getHomeFromDB();

        // 写入Redis缓存（Redis故障时跳过）
        try {
            redisUtil.set(RedisKey.HOME_DATA, homeVO, RedisKey.HOME_DATA_EXPIRE, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis写入首页缓存失败: {}", e.getMessage());
        }

        return homeVO;
    }

    private HomeVO getHomeFromDB() {
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
    @SuppressWarnings("unchecked")
    public List<CategoryVO> getCategoryTree() {
        // 优先从Redis缓存获取（Redis故障时回退到数据库）
        try {
            Object cached = redisUtil.get(RedisKey.CATEGORY_TREE);
            if (cached instanceof List) {
                return (List<CategoryVO>) cached;
            }
        } catch (Exception e) {
            log.warn("Redis获取分类树缓存失败，回退到数据库查询: {}", e.getMessage());
        }

        List<CategoryVO> tree = buildCategoryTreeFromDB();

        // 写入Redis缓存（Redis故障时跳过）
        try {
            redisUtil.set(RedisKey.CATEGORY_TREE, tree, RedisKey.CATEGORY_TREE_EXPIRE, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis写入分类树缓存失败: {}", e.getMessage());
        }

        return tree;
    }

    private List<CategoryVO> buildCategoryTreeFromDB() {
        List<Category> allCategories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder));
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
        // 收集分类ID（含子分类）
        List<Long> categoryIds = null;
        if (categoryId != null) {
            categoryIds = getAllChildCategoryIds(categoryId);
            categoryIds.add(categoryId);
        }

        // 价格区间转为BigDecimal
        BigDecimal minPrice = (priceMin != null && !priceMin.isEmpty()) ? new BigDecimal(priceMin) : null;
        BigDecimal maxPrice = (priceMax != null && !priceMax.isEmpty()) ? new BigDecimal(priceMax) : null;

        // 使用自定义SQL，在数据库层面完成价格排序+区间过滤，避免分页不准
        Page<Spu> spuPage = spuMapper.selectPageWithPriceSort(
                new Page<>(page, size), categoryIds, keyword, minPrice, maxPrice, sort);

        List<ProductVO> voList = toProductVOList(spuPage.getRecords());

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