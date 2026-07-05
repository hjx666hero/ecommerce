package com.ecommerce.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.mall.entity.Sku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SkuMapper extends BaseMapper<Sku> {

    /**
     * CAS原子扣减库存（防止超卖）
     * @return 影响行数，0表示库存不足
     */
    @Update("UPDATE sku SET stock = stock - #{quantity}, locked_stock = COALESCE(locked_stock, 0) + #{quantity} WHERE id = #{id} AND stock >= #{quantity} AND deleted = 0")
    int decreaseStock(@Param("id") Long id, @Param("quantity") int quantity);

    /**
     * CAS原子回滚库存
     * @return 影响行数
     */
    @Update("UPDATE sku SET stock = stock + #{quantity}, locked_stock = COALESCE(locked_stock, 0) - #{quantity} WHERE id = #{id} AND locked_stock >= #{quantity} AND deleted = 0")
    int rollbackStock(@Param("id") Long id, @Param("quantity") int quantity);

    /**
     * CAS原子扣减秒杀库存
     */
    @Update("UPDATE sku SET stock = stock - #{quantity} WHERE id = #{id} AND stock >= #{quantity} AND status = 1 AND deleted = 0")
    int decreaseSeckillStock(@Param("id") Long id, @Param("quantity") int quantity);
}