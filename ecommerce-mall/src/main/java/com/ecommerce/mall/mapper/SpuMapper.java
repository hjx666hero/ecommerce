package com.ecommerce.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.mall.entity.Spu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface SpuMapper extends BaseMapper<Spu> {

    /**
     * 分页查询SPU列表，支持按SKU最低价排序和价格区间过滤（数据库层面）
     */
    @Select("<script>" +
            "SELECT DISTINCT s.* FROM spu s " +
            "INNER JOIN sku k ON s.id = k.spu_id AND k.status = 1 AND k.deleted = 0 " +
            "WHERE s.status = 1 AND s.deleted = 0 " +
            "<if test='categoryIds != null and categoryIds.size() > 0'>" +
            "  AND s.category_id IN " +
            "  <foreach collection='categoryIds' item='cid' open='(' separator=',' close=')'>#{cid}</foreach>" +
            "</if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND s.name LIKE CONCAT('%', #{keyword}, '%')" +
            "</if>" +
            "<if test='priceMin != null'>" +
            "  AND k.price &gt;= #{priceMin}" +
            "</if>" +
            "<if test='priceMax != null'>" +
            "  AND k.price &lt;= #{priceMax}" +
            "</if>" +
            "GROUP BY s.id " +
            "<choose>" +
            "  <when test='sort == \"price_asc\"'>ORDER BY MIN(k.price) ASC</when>" +
            "  <when test='sort == \"price_desc\"'>ORDER BY MIN(k.price) DESC</when>" +
            "  <when test='sort == \"sales\"'>ORDER BY s.sales DESC</when>" +
            "  <otherwise>ORDER BY s.create_time DESC</otherwise>" +
            "</choose>" +
            "</script>")
    Page<Spu> selectPageWithPriceSort(Page<Spu> page,
                                      @Param("categoryIds") List<Long> categoryIds,
                                      @Param("keyword") String keyword,
                                      @Param("priceMin") BigDecimal priceMin,
                                      @Param("priceMax") BigDecimal priceMax,
                                      @Param("sort") String sort);
}