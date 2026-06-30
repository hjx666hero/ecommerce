package com.ecommerce.mall.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.constant.OrderStatus;
import com.ecommerce.common.constant.RedisKey;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.result.ResultCode;
import com.ecommerce.common.util.RedisUtil;
import com.ecommerce.mall.dto.OrderCreateDTO;
import com.ecommerce.mall.entity.*;
import com.ecommerce.mall.mapper.*;
import com.ecommerce.mall.service.OrderService;
import com.ecommerce.mall.vo.OrderItemVO;
import com.ecommerce.mall.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;
    private final SkuMapper skuMapper;
    private final SpuMapper spuMapper;
    private final AddressMapper addressMapper;
    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final MessageMapper messageMapper;
    private final RedisUtil redisUtil;

    @Override
    @Transactional
    public OrderVO createOrder(Long userId, OrderCreateDTO orderCreateDTO) {
        // 分布式锁
        String lockKey = RedisKey.LOCK_ORDER + userId;
        String lockValue = UUID.randomUUID().toString();
        if (!redisUtil.tryLock(lockKey, lockValue, 10)) {
            throw new BusinessException(ResultCode.ORDER_DUPLICATE_SUBMIT);
        }
        try {
            return doCreateOrder(userId, orderCreateDTO);
        } finally {
            redisUtil.releaseLock(lockKey, lockValue);
        }
    }

    private OrderVO doCreateOrder(Long userId, OrderCreateDTO orderCreateDTO) {
        // 收货地址
        Address address = addressMapper.selectOne(new LambdaQueryWrapper<Address>()
                .eq(Address::getId, orderCreateDTO.getAddressId())
                .eq(Address::getUserId, userId));
        if (address == null) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_EXIST);
        }

        // 获取购物车选中商品
        List<Cart> selectedCarts = cartMapper.selectList(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId).eq(Cart::getSelected, 1));
        if (selectedCarts.isEmpty()) {
            throw new BusinessException(400, "购物车中没有选中商品");
        }

        // 校验库存
        List<Long> skuIds = selectedCarts.stream().map(Cart::getSkuId).collect(Collectors.toList());
        List<Sku> skus = skuMapper.selectBatchIds(skuIds);
        Map<Long, Sku> skuMap = skus.stream().collect(Collectors.toMap(Sku::getId, s -> s));
        Map<Long, Spu> spuMap = spuMapper.selectBatchIds(
                selectedCarts.stream().map(Cart::getSpuId).distinct().collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(Spu::getId, s -> s));

        for (Cart cart : selectedCarts) {
            Sku sku = skuMap.get(cart.getSkuId());
            if (sku == null || sku.getStatus() == 0) {
                throw new BusinessException(ResultCode.SKU_NOT_EXIST);
            }
            if (sku.getStock() < cart.getQuantity()) {
                throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
            }
        }

        // 计算金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cart : selectedCarts) {
            Sku sku = skuMap.get(cart.getSkuId());
            totalAmount = totalAmount.add(sku.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
        }

        // 优惠券
        BigDecimal couponDiscount = BigDecimal.ZERO;
        Long couponId = orderCreateDTO.getCouponId();
        if (couponId != null) {
            UserCoupon userCoupon = userCouponMapper.selectOne(new LambdaQueryWrapper<UserCoupon>()
                    .eq(UserCoupon::getUserId, userId)
                    .eq(UserCoupon::getCouponId, couponId)
                    .eq(UserCoupon::getStatus, 0));
            if (userCoupon == null) {
                throw new BusinessException(ResultCode.COUPON_NOT_AVAILABLE);
            }
            Coupon coupon = couponMapper.selectById(couponId);
            if (coupon == null || coupon.getStatus() == 0) {
                throw new BusinessException(ResultCode.COUPON_NOT_EXIST);
            }
            if (LocalDateTime.now().isAfter(coupon.getEndTime())) {
                throw new BusinessException(ResultCode.COUPON_EXPIRED);
            }
            if (totalAmount.compareTo(coupon.getMinAmount()) < 0) {
                throw new BusinessException(ResultCode.COUPON_NOT_MEET_AMOUNT);
            }
            // 计算优惠
            if (coupon.getType() == 1) {
                couponDiscount = coupon.getDiscountValue(); // 满减
            } else if (coupon.getType() == 2) {
                couponDiscount = totalAmount.multiply(BigDecimal.ONE.subtract(coupon.getDiscountValue())); // 折扣
            } else {
                couponDiscount = coupon.getDiscountValue(); // 无门槛
            }
            if (couponDiscount.compareTo(totalAmount) > 0) {
                couponDiscount = totalAmount;
            }

            // 标记优惠券已使用
            userCoupon.setStatus(1);
            userCoupon.setUseTime(LocalDateTime.now());
            userCouponMapper.updateById(userCoupon);
        }

        BigDecimal payAmount = totalAmount.subtract(couponDiscount);

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(couponDiscount);
        order.setPayAmount(payAmount);
        order.setStatus(OrderStatus.PENDING_PAY);
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail());
        order.setRemark(orderCreateDTO.getRemark());
        order.setCouponId(couponId);
        order.setCouponDiscount(couponDiscount);
        orderMapper.insert(order);

        // 创建订单商品
        List<OrderItem> orderItems = new ArrayList<>();
        for (Cart cart : selectedCarts) {
            Sku sku = skuMap.get(cart.getSkuId());
            Spu spu = spuMap.get(cart.getSpuId());
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setOrderNo(order.getOrderNo());
            item.setSpuId(cart.getSpuId());
            item.setSkuId(cart.getSkuId());
            item.setSpuName(spu != null ? spu.getName() : "");
            item.setSkuSpec(sku.getSpecName());
            item.setSkuImage(sku.getImage());
            item.setPrice(sku.getPrice());
            item.setQuantity(cart.getQuantity());
            item.setTotalAmount(sku.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
            orderItems.add(item);

            // 扣减库存
            sku.setStock(sku.getStock() - cart.getQuantity());
            sku.setLockedStock((sku.getLockedStock() != null ? sku.getLockedStock() : 0) + cart.getQuantity());
            skuMapper.updateById(sku);

            // 更新销量
            if (spu != null) {
                spu.setSales((spu.getSales() != null ? spu.getSales() : 0) + cart.getQuantity());
                spuMapper.updateById(spu);
            }
        }
        for (OrderItem item : orderItems) {
            orderItemMapper.insert(item);
        }

        // 清空购物车
        cartMapper.delete(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId).eq(Cart::getSelected, 1));

        // 发送订单消息
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle("订单创建成功");
        message.setContent("您的订单 " + order.getOrderNo() + " 已创建成功，请及时付款。");
        message.setType(2);
        message.setIsRead(0);
        messageMapper.insert(message);

        return toOrderVO(order, orderItems);
    }

    @Override
    public Page<OrderVO> getOrderList(Long userId, Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        Page<Order> orderPage = orderMapper.selectPage(new Page<>(page, size), wrapper);
        List<OrderVO> voList = orderPage.getRecords().stream().map(order -> {
            List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                    .eq(OrderItem::getOrderId, order.getId()));
            return toOrderVO(order, items);
        }).collect(Collectors.toList());

        Page<OrderVO> result = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        result.setRecords(voList);
        return result;
    }

    @Override
    public OrderVO getOrderDetail(Long userId, Long orderId) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId).eq(Order::getUserId, userId));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId()));
        return toOrderVO(order, items);
    }

    @Override
    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId).eq(Order::getUserId, userId));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        if (order.getStatus() != OrderStatus.PENDING_PAY) {
            throw new BusinessException(ResultCode.ORDER_CANNOT_CANCEL);
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 库存回滚
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId()));
        for (OrderItem item : items) {
            Sku sku = skuMapper.selectById(item.getSkuId());
            if (sku != null) {
                sku.setStock(sku.getStock() + item.getQuantity());
                sku.setLockedStock(sku.getLockedStock() - item.getQuantity());
                skuMapper.updateById(sku);
            }
        }

        // 优惠券返还
        if (order.getCouponId() != null) {
            userCouponMapper.update(null, new LambdaUpdateWrapper<UserCoupon>().eq(UserCoupon::getUserId, userId)
                    .eq(UserCoupon::getCouponId, order.getCouponId()).eq(UserCoupon::getStatus, 1)
                    .set(UserCoupon::getStatus, 0).set(UserCoupon::getUseTime, null).set(UserCoupon::getOrderNo, null));
        }
    }

    @Override
    @Transactional
    public void confirmReceive(Long userId, Long orderId) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId).eq(Order::getUserId, userId));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompleteTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void applyRefund(Long userId, Long orderId) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId).eq(Order::getUserId, userId));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        order.setStatus(OrderStatus.REFUNDED);
        orderMapper.updateById(order);

        // 库存回滚
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId()));
        for (OrderItem item : items) {
            Sku sku = skuMapper.selectById(item.getSkuId());
            if (sku != null) {
                sku.setStock(sku.getStock() + item.getQuantity());
                sku.setLockedStock(sku.getLockedStock() - item.getQuantity());
                skuMapper.updateById(sku);
            }
        }
    }

    @Override
    public OrderVO getOrderDetailByOrderNo(Long userId, String orderNo) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo).eq(Order::getUserId, userId));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId()));
        return toOrderVO(order, items);
    }

    @Override
    @Transactional
    public void cancelOrderByOrderNo(Long userId, String orderNo) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo).eq(Order::getUserId, userId));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        cancelOrder(userId, order.getId());
    }

    @Override
    @Transactional
    public void confirmReceiveByOrderNo(Long userId, String orderNo) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo).eq(Order::getUserId, userId));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        confirmReceive(userId, order.getId());
    }

    @Override
    @Transactional
    public void applyRefundByOrderNo(Long userId, String orderNo) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo).eq(Order::getUserId, userId));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        applyRefund(userId, order.getId());
    }

    private String generateOrderNo() {
        return IdUtil.getSnowflakeNextIdStr();
    }

    private OrderVO toOrderVO(Order order, List<OrderItem> items) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setDiscountAmount(order.getDiscountAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setStatus(order.getStatus());
        vo.setStatusText(getStatusText(order.getStatus()));
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverAddress(order.getReceiverAddress());
        vo.setRemark(order.getRemark());
        vo.setCouponId(order.getCouponId());
        vo.setCouponDiscount(order.getCouponDiscount());
        vo.setLogisticsCompany(order.getLogisticsCompany());
        vo.setLogisticsNo(order.getLogisticsNo());
        vo.setPayTime(order.getPayTime());
        vo.setDeliveryTime(order.getDeliveryTime());
        vo.setCompleteTime(order.getCompleteTime());
        vo.setCancelTime(order.getCancelTime());
        vo.setCreateTime(order.getCreateTime());
        if (items != null) {
            vo.setItems(items.stream().map(item -> {
                OrderItemVO iv = new OrderItemVO();
                iv.setId(item.getId());
                iv.setOrderId(item.getOrderId());
                iv.setOrderNo(item.getOrderNo());
                iv.setSpuId(item.getSpuId());
                iv.setSkuId(item.getSkuId());
                iv.setSpuName(item.getSpuName());
                iv.setSkuSpec(item.getSkuSpec());
                iv.setSkuImage(item.getSkuImage());
                iv.setPrice(item.getPrice());
                iv.setQuantity(item.getQuantity());
                iv.setTotalAmount(item.getTotalAmount());
                return iv;
            }).collect(Collectors.toList()));
        }
        return vo;
    }

    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "待付款";
            case 1: return "已支付";
            case 2: return "已发货";
            case 3: return "已完成";
            case 4: return "已取消";
            case 5: return "已退款";
            default: return "未知";
        }
    }
}