package com.ecommerce.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.result.ResultCode;
import com.ecommerce.mall.entity.Cart;
import com.ecommerce.mall.entity.Sku;
import com.ecommerce.mall.entity.Spu;
import com.ecommerce.mall.mapper.CartMapper;
import com.ecommerce.mall.mapper.SkuMapper;
import com.ecommerce.mall.mapper.SpuMapper;
import com.ecommerce.mall.service.CartService;
import com.ecommerce.mall.vo.CartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final SkuMapper skuMapper;
    private final SpuMapper spuMapper;

    @Override
    @Transactional
    public void addToCart(Long userId, Long skuId, Long productId, Integer quantity) {
        // 如果skuId为null，通过productId（spuId）查找第一个有效SKU
        if (skuId == null) {
            if (productId == null) {
                throw new BusinessException(ResultCode.SKU_NOT_EXIST);
            }
            Sku defaultSku = skuMapper.selectOne(new LambdaQueryWrapper<Sku>()
                    .eq(Sku::getSpuId, productId)
                    .eq(Sku::getStatus, 1)
                    .orderByAsc(Sku::getId)
                    .last("LIMIT 1"));
            if (defaultSku == null) {
                throw new BusinessException(ResultCode.SKU_NOT_EXIST);
            }
            skuId = defaultSku.getId();
        }

        Sku sku = skuMapper.selectById(skuId);
        if (sku == null || sku.getStatus() == 0) {
            throw new BusinessException(ResultCode.SKU_NOT_EXIST);
        }
        // 检查是否已存在
        Cart existCart = cartMapper.selectOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId).eq(Cart::getSkuId, skuId));
        if (existCart != null) {
            existCart.setQuantity(existCart.getQuantity() + quantity);
            cartMapper.updateById(existCart);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setSkuId(skuId);
            cart.setSpuId(sku.getSpuId());
            cart.setQuantity(quantity);
            cart.setSelected(1);
            cartMapper.insert(cart);
        }
    }

    @Override
    @Transactional
    public void updateQuantity(Long userId, Long cartId, Integer quantity) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CART_ITEM_NOT_EXIST);
        }
        if (quantity <= 0) {
            cartMapper.deleteById(cartId);
        } else {
            cart.setQuantity(quantity);
            cartMapper.updateById(cart);
        }
    }

    @Override
    @Transactional
    public void deleteCart(Long userId, Long cartId) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CART_ITEM_NOT_EXIST);
        }
        cartMapper.deleteById(cartId);
    }

    @Override
    @Transactional
    public void deleteBatch(Long userId, List<Long> cartIds) {
        if (cartIds == null || cartIds.isEmpty()) return;
        cartMapper.delete(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId).in(Cart::getId, cartIds));
    }

    @Override
    @Transactional
    public void deleteAll(Long userId) {
        cartMapper.delete(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId));
    }

    @Override
    @Transactional
    public void selectAll(Long userId, Boolean selected) {
        cartMapper.update(null, new LambdaUpdateWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .set(Cart::getSelected, selected ? 1 : 0));
    }

    @Override
    @Transactional
    public void selectOne(Long userId, Long cartId, Boolean selected) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CART_ITEM_NOT_EXIST);
        }
        cart.setSelected(selected ? 1 : 0);
        cartMapper.updateById(cart);
    }

    @Override
    public List<CartVO> getCartList(Long userId) {
        List<Cart> carts = cartMapper.selectList(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId).orderByDesc(Cart::getCreateTime));
        return toCartVOList(carts);
    }

    @Override
    public List<CartVO> getSelectedCartList(Long userId) {
        List<Cart> carts = cartMapper.selectList(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId).eq(Cart::getSelected, 1));
        return toCartVOList(carts);
    }

    private List<CartVO> toCartVOList(List<Cart> carts) {
        if (carts.isEmpty()) return List.of();
        List<Long> skuIds = carts.stream().map(Cart::getSkuId).collect(Collectors.toList());
        List<Long> spuIds = carts.stream().map(Cart::getSpuId).distinct().collect(Collectors.toList());

        Map<Long, Sku> skuMap = skuMapper.selectBatchIds(skuIds).stream()
                .collect(Collectors.toMap(Sku::getId, s -> s));
        Map<Long, Spu> spuMap = spuMapper.selectBatchIds(spuIds).stream()
                .collect(Collectors.toMap(Spu::getId, s -> s));

        return carts.stream().map(cart -> {
            CartVO vo = new CartVO();
            vo.setId(cart.getId());
            vo.setUserId(cart.getUserId());
            vo.setSkuId(cart.getSkuId());
            vo.setSpuId(cart.getSpuId());
            vo.setQuantity(cart.getQuantity());
            vo.setSelected(cart.getSelected());
            vo.setCreateTime(cart.getCreateTime());
            Sku sku = skuMap.get(cart.getSkuId());
            if (sku != null) {
                vo.setSkuSpec(sku.getSpecName());
                vo.setPrice(sku.getPrice());
                vo.setStock(sku.getStock());
            }
            Spu spu = spuMap.get(cart.getSpuId());
            if (spu != null) {
                vo.setSpuName(spu.getName());
                vo.setSpuImage(spu.getMainImage());
            }
            return vo;
        }).collect(Collectors.toList());
    }
}