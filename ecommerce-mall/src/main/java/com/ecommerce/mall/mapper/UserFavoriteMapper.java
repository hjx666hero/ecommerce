package com.ecommerce.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.mall.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {
}