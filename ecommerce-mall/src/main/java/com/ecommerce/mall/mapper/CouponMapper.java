package com.ecommerce.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.mall.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    /**
     * CAS原子扣减优惠券库存
     * @return 影响行数，0表示库存不足
     */
    @Update("UPDATE coupon SET receive_count = receive_count + 1 WHERE id = #{id} AND receive_count < total_count AND status = 1 AND deleted = 0")
    int incrReceiveCount(@Param("id") Long id);
}