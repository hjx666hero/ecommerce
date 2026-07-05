package com.ecommerce.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.mall.entity.LocalMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface LocalMessageMapper extends BaseMapper<LocalMessage> {

    /**
     * CAS更新消息状态为处理中，防止重复处理
     */
    @Update("UPDATE local_message SET status = 1, retry_count = retry_count + 1 WHERE id = #{id} AND status = 0")
    int markProcessing(@Param("id") Long id);
}
